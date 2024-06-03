package kol.trade.service.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.eventbus.AsyncEventBus;

import io.contek.invoker.commons.actor.http.ParsedHttpException;
import io.contek.invoker.okx.api.common._Instrument;
import kol.common.model.MessageType;
import kol.common.service.BaseService;
import kol.common.utils.JsonUtils;
import kol.exchange.service.OkxService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.entity.TradeOrder;
import kol.trade.entity.TraderOrderError;
import kol.trade.enums.CapitalModeEnum;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.PositionRepo;
import kol.trade.repo.TradeOrderRepo;
import kol.trade.repo.TraderOrderErrorRepo;
import lombok.extern.slf4j.Slf4j;

/**
 * 开仓
 *
 * @author kent
 */
@Slf4j
@Service
public class OpenPositionService extends BaseService {

    /**
     * 容错交易所余额与KOL系统的误差值
     */
    private static final BigDecimal OFFSET = new BigDecimal("0.98");
    private static final Integer POOL_SIZE = 5;

    final PositionRepo positionRepo;
    final InvestmentRepo investRepo;
    final InvestmentRepo investmentRepo;
    final TradeOrderRepo tradeOrderRepo;
    final OkxService exchangeUtil;
    final PostOrderService postOrderService;
    final TraderOrderErrorRepo traderOrderErrorRepo;
    final AsyncEventBus asyncEventBus;
    @Autowired
    StrategyDispatchService strategyDispatchService;

    public OpenPositionService(PositionRepo positionRepo, InvestmentRepo investRepo, InvestmentRepo investmentRepo,
                               TradeOrderRepo tradeOrderRepo, OkxService exchangeUtil, PostOrderService postOrderService, TraderOrderErrorRepo traderOrderErrorRepo, AsyncEventBus asyncEventBus) {
        this.positionRepo = positionRepo;
        this.investRepo = investRepo;
        this.investmentRepo = investmentRepo;
        this.tradeOrderRepo = tradeOrderRepo;
        this.exchangeUtil = exchangeUtil;
        this.postOrderService = postOrderService;
        this.traderOrderErrorRepo = traderOrderErrorRepo;
        this.asyncEventBus = asyncEventBus;
    }

    /**
     * 开仓
     *
     * @param cmd
     */
    public void openPosition(OrderCmd cmd) {
        //判断是否存在跟单
        List<Investment> investList = investRepo.findByStrategyIdAndIsPauseAndIsEnd(cmd.getStrategyId(), false, false);
        if (CollectionUtils.isEmpty(investList)) {
            return;
        }
        //判断止盈止损是否合法

        List<Position> positionList
                = positionRepo.findStrategyPosition(cmd.getStrategyId(), PositionStatusEnum.OPEN, cmd.getTradeType().getPositionSide(), cmd.getSymbol());
        Map<Long, Position> positionMap = positionList.stream()
                .collect(Collectors.toMap(Position::getInvestmentId, position -> position));

        List<TraderOrderError> orderErrorList = new ArrayList<>();
        try {
            investList.stream()
                    .forEach(invest -> {
                        Position position = positionMap.get(invest.getId());
                        TraderOrderError traderOrderError = this.execute(cmd, invest, position);
                        if (Objects.nonNull(traderOrderError)) {
                            orderErrorList.add(traderOrderError);
                        }
                    });
            strategyDispatchService.dispatch(cmd);
            if (!CollectionUtils.isEmpty(orderErrorList)) {
                traderOrderErrorRepo.saveAll(orderErrorList);
                throw new RuntimeException(orderErrorList.size() + "位跟单用户开仓失败");
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.info(cmd.toString());
            log.error("openPosition：", ex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public TraderOrderError execute(OrderCmd cmd, Investment invest, Position position) {
        ExchangeEnum exchangeEnum = position != null ? position.getExchange() : cmd.getExchange();
        TraderOrderError orderError = null;
        try {
            TradeOrder tradeOrder = postOrder(cmd, invest);
            Position backPosition = this.savePosition(tradeOrder, position, cmd);
            this.saveInvestment(tradeOrder, invest);
            this.asyncEventBus.post(backPosition);
            log.info("订单ID {} 仓位ID {} 跟单ID {}", tradeOrder.getId(), backPosition.getId(), invest.getId());
        } catch (ParsedHttpException ex) {
            log.info(cmd.toString());
            log.error("open-execute【ParsedHttpException】：{}", ex.getParsedEntity());
            log.error("open-execute【ParsedHttpException】 code: {}, msg: {}", ex.getCode(), ex.getMessage());
            orderError = this.buildOrderError(cmd, ex.getMessage(), invest);
            handleMessage(invest.getAccountId(), exchangeEnum, JsonUtils.objectToJson(ex.getParsedEntity()), MessageType.OPEN_FAILD);
        } catch (Exception ex) {
            log.info(cmd.toString());
            log.error("open-execute【Exception】：{}", ex);
            orderError = this.buildOrderError(cmd, ex.getMessage(), invest);
            handleMessage(invest.getAccountId(), exchangeEnum, ex.getMessage(), MessageType.OPEN_FAILD);
        }
        return orderError;
    }

    /**
     * 执行交易所开平仓
     *
     * @param cmd
     * @param invest
     * @return
     */
    private TradeOrder postOrder(OrderCmd cmd, Investment invest) {
        BigDecimal vol = calVolume(invest, cmd);
        if (!invest.getApiKeyId().equals(-1L) && Boolean.TRUE.equals(invest.getIsReal())) {
            return postOrderService.postOrder(invest, cmd, vol);
        } else {
            return simulateTrade(cmd, invest, vol);
        }
    }

    /**
     * 保存仓位信息
     */
    private Position savePosition(TradeOrder tradeOrder, Position position, OrderCmd cmd) {
        //position=null第一次开仓，不等于null加仓
        if (position == null) {
            position = buildPosition(tradeOrder);
        } else {
            BigDecimal totalVol = tradeOrder.getVol().add(position.getEntryVol());
            //计算平均价格
            BigDecimal avgPrice = tradeOrder.getVol().multiply(tradeOrder.getAvgPrice())
                    .add(position.getEntryVol().subtract(position.getExitVol()).multiply(position.getEntryPrice()))
                    .divide(totalVol.subtract(position.getExitVol()), 10, RoundingMode.UP);
            BigDecimal totalFee = tradeOrder.getFee().add(position.getFee());
            BigDecimal profit = position.getProfit().subtract(tradeOrder.getFee());
            position.setProfit(profit);
            position.setEntryVol(totalVol);
            position.setEntryPrice(avgPrice);
            position.setFee(totalFee);
        }
        position.setStopLossPrice(cmd.getStopLossPrice());
        position.setStopGainPrice(cmd.getStopGainPrice());
        return positionRepo.save(position);
    }

    /**
     * 保存投资信息
     *
     * @param tradeOrder
     * @param invest
     */
    private void saveInvestment(TradeOrder tradeOrder, Investment invest) {
        //计算已用金额
        BigDecimal useAmount = tradeOrder.getVol()
                .multiply(tradeOrder.getAvgPrice())
                .divide(tradeOrder.getLeverage(), 10, RoundingMode.DOWN);
        BigDecimal useBalance = invest.getUseBalance().add(useAmount);
        invest.setUseBalance(useBalance);
        invest.setBalance(invest.getBalance().subtract(tradeOrder.getFee()));
        investmentRepo.save(invest);
    }

    /**
     * 构建仓位信息
     *
     * @return
     */
    private Position buildPosition(TradeOrder tradeOrder) {
        Position position = new Position();
        BeanUtils.copyProperties(tradeOrder, position);
        position.setEntryPrice(tradeOrder.getAvgPrice());
        position.setEntryVol(tradeOrder.getVol());
        position.setEntryTime(LocalDateTime.now());
        position.setExitPrice(BigDecimal.ZERO);
        position.setExitVol(BigDecimal.ZERO);
        position.setPositionSide(tradeOrder.getTradeType().getPositionSide());
        position.setProfit(position.getProfit().subtract(tradeOrder.getFee()));
        position.setStatus(PositionStatusEnum.OPEN);
        position.setIsSettlement(Boolean.FALSE);
        return position;
    }

    /**
     * 计算开仓张数
     *
     * @param invest
     * @param cmd
     * @return
     */
    private BigDecimal calVolume(Investment invest, OrderCmd cmd) {
        BigDecimal price = exchangeUtil.getPrice(cmd.getSymbol());
        _Instrument instrument = exchangeUtil.getInstrumentMap().get(cmd.getSymbol());
        //TODO 计算开仓量(U本位与币本位)
        BigDecimal vol;
        if (CapitalModeEnum.REMAIND.equals(cmd.getCapitalMode())) {
            vol = invest.getBalance()
                    .subtract(invest.getUseBalance())
                    .multiply(cmd.getLeverage())
                    .multiply(cmd.getPositionRatio())
                    .multiply(OFFSET);
        } else {
            BigDecimal balance = invest.getPrincipal().multiply(cmd.getPositionRatio());
            balance = balance.compareTo(invest.getBalance().subtract(invest.getUseBalance())) == 1
                    ? invest.getBalance().subtract(invest.getUseBalance()) : balance;
            vol = balance.multiply(cmd.getLeverage()).multiply(OFFSET);
        }

        if (instrument.ctType.equals("linear")) {
            vol = vol.divide(price, 10, RoundingMode.DOWN);
        }
        Boolean result = vol.compareTo(BigDecimal.ZERO) > 0;
        Assert.isTrue(result, "资金已使用完，无法继续开仓");
        return vol;
    }

    private TradeOrder simulateTrade(OrderCmd cmd, Investment invest, BigDecimal vol) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setInvestmentId(invest.getId());
        tradeOrder.setAccountId(invest.getAccountId());
        tradeOrder.setStrategyId(invest.getStrategyId());
        tradeOrder.setPositionNo(cmd.getPositionNo());
        tradeOrder.setMarket(cmd.getMarket());
        tradeOrder.setSymbol(cmd.getSymbol());
        tradeOrder.setExchange(cmd.getExchange());
        tradeOrder.setLeverage(cmd.getLeverage());
        tradeOrder.setTradeType(cmd.getTradeType());
        tradeOrder.setVol(vol);
        tradeOrder.setExchangeOrderId("模拟交易没有订单ID");
        tradeOrder.setIsReal(false);
        BigDecimal price = exchangeUtil.getPrice(cmd.getSymbol());
        tradeOrder.setAvgPrice(price);
        BigDecimal fee = tradeOrder.getVol().multiply(tradeOrder.getAvgPrice()).multiply(new BigDecimal("0.0005"));
        tradeOrder.setFee(fee);
        tradeOrderRepo.save(tradeOrder);
        return tradeOrder;
    }

    private TraderOrderError buildOrderError(OrderCmd cmd, String msg, Investment invest) {
        TraderOrderError entity = new TraderOrderError();
        entity.setStrategyId(cmd.getStrategyId());
        entity.setAccountId(invest.getAccountId());
        entity.setApiKeyId(invest.getApiKeyId());
        entity.setTradeType(cmd.getTradeType());
        entity.setErrorMessage(msg);
        entity.setPositionNo(cmd.getPositionNo());
        entity.setInvestStr(JsonUtils.objectToJson(invest));
        entity.setRequestCmd(JsonUtils.objectToJson(cmd));
        return entity;
    }
}
