package kol.trade.service.trading;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.AsyncEventBus;
import io.contek.invoker.commons.actor.http.ParsedHttpException;
import io.contek.invoker.okx.api.common._Position;
import kol.account.model.Account;
import kol.account.repo.AccountInfoRepo;
import kol.account.repo.AccountRebateRelationRepo;
import kol.account.repo.AccountRepo;
import kol.account.service.AccountRebateService;
import kol.common.model.MessageType;
import kol.common.service.BaseService;
import kol.common.utils.EmailTool;
import kol.common.utils.JsonUtils;
import kol.config.model.Config;
import kol.config.service.ConfigService;
import kol.exchange.service.OkxService;
import kol.money.model.MoneyRecord;
import kol.money.repo.MoneyRecordRepo;
import kol.money.repo.WalletRepo;
import kol.money.service.WalletService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.*;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import kol.trade.enums.TradeTypeEnum;
import kol.trade.repo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 平仓
 *
 * @author kent
 */
@Slf4j
@Service
public class ClosePositionService extends BaseService {

    private static final Integer POOL_SIZE = 5;

    final PositionRepo positionRepo;
    final InvestmentRepo investRepo;
    final StrategyRepo strategyRepo;
    final StrategySnapshotRepo strategySnapshotRepo;
    final TradeOrderRepo tradeOrderRepo;
    final InvestSnapshotRepo investmentSnapshotRepo;
    final OkxService exchangeUtil;
    final MoneyRecordRepo moneyRecordRepo;
    final TraderOrderErrorRepo traderOrderErrorRepo;
    final WalletRepo walletRepo;
    final AccountInfoRepo accountInfoRepo;
    final AccountRebateRelationRepo rebateRelationRepo;
    final AsyncEventBus asyncEventBus;

    @Resource
    private AccountRebateService accountRebateService;
    @Resource
    private PostOrderService postOrderService;
    @Resource
    private ConfigService configService;
    @Resource
    private WalletService walletService;
    @Autowired
    StrategyDispatchService strategyDispatchService;
    @Autowired
    EmailTool emailTool;
    @Autowired
    AccountRepo accountRepo;

    public ClosePositionService(PositionRepo positionRepo, InvestmentRepo investRepo, StrategyRepo strategyRepo,
                                StrategySnapshotRepo strategySnapshotRepo, TradeOrderRepo tradeOrderRepo,
                                InvestSnapshotRepo investmentSnapshotRepo, OkxService exchangeUtil, MoneyRecordRepo moneyRecordRepo,
                                TraderOrderErrorRepo traderOrderErrorRepo, WalletRepo walletRepo, AccountInfoRepo accountInfoRepo,
                                AccountRebateRelationRepo rebateRelationRepo, AsyncEventBus asyncEventBus) {
        this.positionRepo = positionRepo;
        this.investRepo = investRepo;
        this.strategyRepo = strategyRepo;
        this.strategySnapshotRepo = strategySnapshotRepo;
        this.tradeOrderRepo = tradeOrderRepo;
        this.investmentSnapshotRepo = investmentSnapshotRepo;
        this.exchangeUtil = exchangeUtil;
        this.moneyRecordRepo = moneyRecordRepo;
        this.traderOrderErrorRepo = traderOrderErrorRepo;
        this.walletRepo = walletRepo;
        this.accountInfoRepo = accountInfoRepo;
        this.rebateRelationRepo = rebateRelationRepo;
        this.asyncEventBus = asyncEventBus;
    }

    public void closePosition(OrderCmd cmd) {
        List<Position> positionList = positionRepo.findStrategyPosition(cmd.getStrategyId(),
                PositionStatusEnum.OPEN,
                cmd.getTradeType().getPositionSide(),
                cmd.getSymbol());
        if (CollectionUtils.isEmpty(positionList)) {
            return;
        }

        List<Long> investIds = positionList.parallelStream().map(Position::getInvestmentId).distinct().toList();
        List<Investment> investList = investRepo.findByIdIn(investIds);

        if (CollectionUtils.isEmpty(investList)) {
            return;
        }

        Map<Long, Investment> investMap = investList.parallelStream()
                .filter(invest -> invest.getStrategyId().equals(cmd.getStrategyId()))
                .collect(Collectors.toMap(Investment::getId, invest -> invest));

        List<TraderOrderError> orderErrorList = new ArrayList<>();

        positionList.parallelStream().forEach(position -> {
            Investment invest = investMap.get(position.getInvestmentId());
            TraderOrderError traderOrderError = this.execute(cmd, invest, position);
            if (Objects.nonNull(traderOrderError)) {
                orderErrorList.add(traderOrderError);
            }
        });
        strategyDispatchService.dispatch(cmd);
        if (!CollectionUtils.isEmpty(orderErrorList)) {
            traderOrderErrorRepo.saveAll(orderErrorList);
            throw new RuntimeException(orderErrorList.size() + "位跟单用户平仓失败");
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public TraderOrderError execute(OrderCmd cmd, Investment invest, Position position) {
        ExchangeEnum exchangeEnum = position != null ? position.getExchange() : cmd.getExchange();
        TraderOrderError orderError = null;
        try {
            TradeOrder tradeOrder = this.postOrder(cmd, invest, position);
            this.savePosition(tradeOrder, position);
            this.saveInvestment(tradeOrder, position, invest);
            this.saveMoneyRecord(position);
            this.saveWallet(position);
            this.saveStrategy(position, invest);
            this.asyncEventBus.post(position);
        } catch (ParsedHttpException ex) {
            log.info(cmd.toString());
            log.error("close-execute【ParsedHttpException】：{}", ex.getParsedEntity());
            log.error("close-execute【ParsedHttpException】 code: {}, msg: {}", ex.getCode(), ex.getMessage());
            retryClosePosition(ex.getParsedEntity(), cmd, invest, position);
            handleMessage(invest.getAccountId(), exchangeEnum, JsonUtils.objectToJson(ex.getParsedEntity()), MessageType.CLOSE_FAILD);
        } catch (Exception ex) {
            log.info(cmd.toString());
            log.error("close-execute【Exception】：{}", ex);
            orderError = this.buildOrderError(cmd, ex.getMessage(), invest);
            handleMessage(invest.getAccountId(), exchangeEnum, ex.getMessage(), MessageType.CLOSE_FAILD);
        }
        return orderError;
    }

    private TradeOrder postOrder(OrderCmd cmd, Investment invest, Position position) {
        BigDecimal vol = calVolume(cmd, position);
        if (vol.compareTo(BigDecimal.ZERO) == 1) {
            cmd.setPositionNo(position.getPositionNo());
            cmd.setLeverage(position.getLeverage());
            if (!invest.getApiKeyId().equals(-1L) && Boolean.TRUE.equals(invest.getIsReal())) {
                return postOrderService.postOrder(invest, cmd, vol);
            }
        }
        return simulateTrade(cmd, position, vol);
    }

    /**
     * 保存仓位信息
     *
     * @param tradeOrder
     * @param position
     */
    private void savePosition(TradeOrder tradeOrder, Position position) {
        BigDecimal exitVol = position.getExitVol().add(tradeOrder.getVol());
        BigDecimal exitPrice = position.getExitPrice()
                .multiply(position.getExitVol())
                .add(tradeOrder.getAvgPrice().multiply(tradeOrder.getVol()))
                .divide(exitVol, 10, RoundingMode.DOWN);
        BigDecimal totalFee = position.getFee().add(tradeOrder.getFee());

        position.setExitVol(exitVol);
        position.setExitPrice(exitPrice);
        position.setFee(totalFee);
        position.setExitTime(LocalDateTime.now());
        this.calProfit(position, tradeOrder);
        PositionStatusEnum positionStatus = exitVol.compareTo(position.getEntryVol()) == 0 ? PositionStatusEnum.CLOSE
                : PositionStatusEnum.OPEN;
        position.setStatus(positionStatus);
        position.setFollowCost(BigDecimal.ZERO);
        if (PositionStatusEnum.CLOSE.equals(positionStatus)) {
            BigDecimal followCost = this.calFollowCost(position.getProfit());
            position.setFollowCost(followCost);
        }
        position = positionRepo.save(position);
    }

    /**
     * 保存投资信息
     *
     * @param tradeOrder
     * @param position
     * @param invest
     */
    private void saveInvestment(TradeOrder tradeOrder, Position position, Investment invest) {
        if (tradeOrder.getVol().compareTo(BigDecimal.ZERO) < 1) {
            return;
        }
        BigDecimal useMargin = position.getEntryPrice().multiply(tradeOrder.getVol());

        BigDecimal profit;
        if (PositionSideEnum.LONG == position.getPositionSide()) {
            // 收益=（平仓价格-开仓价）*平仓量-平仓手续费
            profit = tradeOrder.getAvgPrice()
                    .subtract(position.getEntryPrice())
                    .multiply(tradeOrder.getVol())
                    .subtract(tradeOrder.getFee());
        } else {
            // 收益=（开仓价-平仓价格）*平仓量-平仓手续费
            profit = position.getEntryPrice()
                    .subtract(tradeOrder.getAvgPrice())
                    .multiply(tradeOrder.getVol())
                    .subtract(tradeOrder.getFee());
        }

        BigDecimal useBalance = invest.getUseBalance()
                .subtract(useMargin.divide(position.getLeverage(), 10, RoundingMode.DOWN));
        useBalance = useBalance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : useBalance;
        BigDecimal balance = profit.add(invest.getBalance());

        invest.setUseBalance(useBalance);
        invest.setBalance(balance);
        investRepo.save(invest);

        if (PositionStatusEnum.CLOSE.equals(position.getStatus())) {
            addSnapshot(invest);
        }
    }

    /**
     * 保存策略
     *
     * @param position
     * @param invest
     */
    private void saveStrategy(Position position, Investment invest) {
        // 模拟
        if (Boolean.FALSE.equals(position.getIsReal()) && invest.getApiKeyId().equals(-1L)
                && PositionStatusEnum.CLOSE.equals(position.getStatus())) {
            Optional<Strategy> strategyOptional = strategyRepo.findById(position.getStrategyId());
            Strategy strategy = strategyOptional.get();

            if (position.getProfit().compareTo(BigDecimal.ZERO) > -1) {
                strategy.setWinCount(strategy.getWinCount() + 1);
            } else {
                strategy.setLossCount(strategy.getLossCount() + 1);
            }
            strategy.setBalance(invest.getBalance());

            // 计算策略历史最大盈利
            BigDecimal maxBalance = strategy.getBalance().compareTo(strategy.getMaxBalance()) > 0
                    ? strategy.getBalance()
                    : strategy.getMaxBalance();
            strategy.setMaxBalance(maxBalance);

            // 计算盈利率
            BigDecimal profitRate = strategy.getBalance()
                    .subtract(strategy.getPrincipal())
                    .divide(strategy.getPrincipal(), 5, RoundingMode.DOWN);
            strategy.setProfitRate(profitRate);

            // 计算最大回测
            BigDecimal maximumDrawdown = strategy.getMaxBalance()
                    .subtract(strategy.getBalance())
                    .divide(strategy.getMaxBalance(), 5, RoundingMode.DOWN);
            maximumDrawdown = strategy.getMaximumDrawdown().compareTo(maximumDrawdown) > 0
                    ? strategy.getMaximumDrawdown()
                    : maximumDrawdown;
            strategy.setMaximumDrawdown(maximumDrawdown);

            strategyRepo.save(strategy);

            StrategySnapshot strategySnapshot = new StrategySnapshot();
            BeanUtils.copyProperties(strategy, strategySnapshot);
            strategySnapshot.setStrategyId(strategy.getId());
            strategySnapshot.setId(null);
            strategySnapshotRepo.save(strategySnapshot);
        }
    }

    /**
     * 保存钱包记录
     *
     * @param position
     */
    private void saveMoneyRecord(Position position) {
        String stateMode = configService.getValue(Config.KeyEnum.STATE_MODE);
        if (Boolean.TRUE.equals(position.getIsReal())
                && PositionStatusEnum.CLOSE.equals(position.getStatus())
                && position.getProfit().compareTo(BigDecimal.ZERO) != 0
                && "PER_ORDER".equals(stateMode)) {

            MoneyRecord moneyRecord = new MoneyRecord();
            moneyRecord.setAccountId(position.getAccountId());
            moneyRecord.setAmount(position.getFollowCost().multiply(new BigDecimal("-1")));
            if (position.getProfit().compareTo(BigDecimal.ZERO) == 1) {
                moneyRecord.setType(MoneyRecord.Type.TRADE_COST);
                moneyRecord.setComment("交易盈利扣费");
            } else {
                moneyRecord.setType(MoneyRecord.Type.TRADE_COST);
                moneyRecord.setComment("交易亏损返费");
            }
            moneyRecordRepo.save(moneyRecord);
        }
    }

    private void saveWallet(Position position) {
        String stateMode = configService.getValue(Config.KeyEnum.STATE_MODE);
        if (Boolean.TRUE.equals(position.getIsReal())
                && PositionStatusEnum.CLOSE.equals(position.getStatus())
                && position.getProfit().compareTo(BigDecimal.ZERO) != 0
                && "PER_ORDER".equals(stateMode)) {
            walletService.updateFollowCostAmount(position.getFollowCost(), position.getAccountId());
        }
    }

    /**
     * 计算平仓量
     *
     * @param cmd
     * @param position
     * @return
     */
    private BigDecimal calVolume(OrderCmd cmd, Position position) {
        return position.getEntryVol().subtract(position.getExitVol()).multiply(cmd.getPositionRatio());
    }

    /**
     * 计算收益和收益率
     *
     * @param position
     */
    private void calProfit(Position position, TradeOrder tradeOrder) {

        BigDecimal profit;
        if (PositionSideEnum.LONG == position.getPositionSide()) {
            // 收益=（平仓价格-开仓价）*平仓量+上次收益-平仓手续费
            profit = tradeOrder.getAvgPrice()
                    .subtract(position.getEntryPrice())
                    .multiply(tradeOrder.getVol())
                    .add(position.getProfit())
                    .subtract(tradeOrder.getFee());
        } else {
            // 收益=（开仓价-平仓价格）*平仓量+上次收益-平仓手续费
            profit = position.getEntryPrice()
                    .subtract(tradeOrder.getAvgPrice())
                    .multiply(tradeOrder.getVol())
                    .add(position.getProfit())
                    .subtract(tradeOrder.getFee());
        }
        position.setProfit(profit);

        // 收益率=收益/本金
        BigDecimal margin = position.getEntryPrice()
                .multiply(position.getEntryVol())
                .divide(position.getLeverage(), 10, RoundingMode.DOWN);
        BigDecimal profitRate = profit.divide(margin, 5, RoundingMode.DOWN);
        position.setProfitRate(profitRate);

    }

    /**
     * 交易盈利计算跟单手续费
     *
     * @return
     */
    private BigDecimal calFollowCost(BigDecimal profit) {
        BigDecimal followCost = profit.multiply(configService.getFollowCostRatio());
        return followCost;
    }

    /**
     * 模拟开单
     *
     * @param cmd
     * @param position
     * @return
     */
    private TradeOrder simulateTrade(OrderCmd cmd, Position position, BigDecimal vol) {
        TradeOrder tradeOrder = new TradeOrder();
        tradeOrder.setInvestmentId(position.getInvestmentId());
        tradeOrder.setAccountId(position.getAccountId());
        tradeOrder.setStrategyId(position.getStrategyId());
        tradeOrder.setPositionNo(position.getPositionNo());
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

    /**
     * 停止跟单时清仓
     *
     * @param invest
     */
    public void closePosition(Investment invest) {
        List<Position> positionList = positionRepo.findPositionList(invest.getId(), invest.getStrategyId());
        if (CollectionUtils.isEmpty(positionList)) {
            return;
        }

        positionList.parallelStream().forEach(p -> {
            OrderCmd cmd = positionToOrderCmd(p);
            this.execute(cmd, invest, p);
        });
    }

    /**
     * 单个仓位平仓
     *
     * @param uid
     * @param positionId
     */
    public void closePosition(Long uid, Long positionId) {
        Optional<Position> optional = positionRepo.findByAccountIdAndId(uid, positionId);
        Assert.isTrue(optional.isPresent(), "仓位不存在");
        Optional<Investment> investOptional = investRepo.findById(optional.get().getInvestmentId());
        OrderCmd cmd = positionToOrderCmd(optional.get());
        this.execute(cmd, investOptional.get(), optional.get());
    }

    /**
     * 用户清仓
     *
     * @param uid
     */
    @Transactional(rollbackFor = Exception.class)
    public void closePosition(Long uid) {
        investRepo.findByIsEndAndAccountId(false, uid).forEach(invest -> {
            closePosition(invest);
        });
    }

    /**
     * 添加投资快照
     *
     * @param investment
     */
    public void addSnapshot(Investment investment) {
        InvestSnapshot snapshot = new InvestSnapshot();
        BeanUtils.copyProperties(investment, snapshot);
        snapshot.setInvestId(investment.getId());
        snapshot.setId(null);
        BigDecimal profitRate = investment.getBalance()
                .subtract(investment.getPrincipal())
                .divide(investment.getPrincipal(), 5, RoundingMode.DOWN);
        snapshot.setProfitRate(profitRate);
        investmentSnapshotRepo.save(snapshot);
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

    private void retryClosePosition(Object parsedEntity, OrderCmd cmd, Investment invest, Position position) {
        JsonNode root = JsonUtils.getJsonNode(JsonUtils.objectToJson(parsedEntity));
        JsonNode error = root.get("data").get(0);
        if (error.get("sCode").asText().equals("51112")
                || error.get("sCode").asText().equals("51169")) {
            log.info("客户手动平仓 客户id {}  跟单id {} 仓位id {}", invest.getAccountId(), invest.getId(), position.getId());
            // 客户实际仓位小于跟单仓位，客户存在手动平仓情况
            Account account = accountRepo.findById(invest.getAccountId()).get();
            ExchangeKey apiKey = JsonUtils.jsonToPojo(invest.getApiKey(), ExchangeKey.class);

            List<_Position> positions = exchangeUtil.getCurrentPosition(apiKey, cmd.getSymbol());
            String posSide = (cmd.getTradeType() == TradeTypeEnum.CLOSE_LONG ? "long" : "short");
            // 处理剩余未平仓部分
            Optional<_Position> positionOptional = positions.stream().filter(p -> p.posSide.equals(posSide)).findAny();
            BigDecimal offsetVol;
            if (positionOptional.isPresent() && StringUtils.isNotBlank(positionOptional.get().availPos)) {
                BigDecimal availPos = exchangeUtil.szToVolume(Integer.parseInt(positionOptional.get().availPos), cmd.getSymbol());
                //剩余仓位与交易所仓位偏移量
                offsetVol = position.getEntryVol().subtract(position.getExitVol()).subtract(availPos);
            } else {
                offsetVol = position.getEntryVol().subtract(position.getExitVol());
            }

            //处理投资已使用金额
            TradeOrder tradeOrder = this.simulateTrade(cmd, position, offsetVol);
            this.saveInvestment(tradeOrder, position, invest);
            this.savePosition(tradeOrder, position);

            this.execute(cmd, invest, position);
            //发送邮件告警
            emailTool.sendMessage(account.getEmail(), "检测到账户存在客户手动交易情况通知", "为保证跟单信号正常，请勿手动操作账户");
        }

    }

    /**
     * 仓位转换开仓信号
     *
     * @param position
     * @return
     */
    private OrderCmd positionToOrderCmd(Position position) {
        OrderCmd cmd = new OrderCmd();
        cmd.setStrategyId(position.getId());
        cmd.setSymbol(position.getSymbol());
        TradeTypeEnum tradeType = PositionSideEnum.LONG.equals(position.getPositionSide()) ? TradeTypeEnum.CLOSE_LONG
                : TradeTypeEnum.CLOSE_SHORT;
        cmd.setTradeType(tradeType);
        cmd.setPositionRatio(BigDecimal.ONE);
        cmd.setLeverage(position.getLeverage());
        cmd.setMarket(position.getMarket());
        cmd.setPositionNo(position.getPositionNo());
        cmd.setExchange(position.getExchange());
        return cmd;
    }

    /**
     * 计算平仓均价
     *
     * @param position
     * @param price
     * @param vol
     * @return
     */
    private BigDecimal calExitAvgPrice(Position position, BigDecimal price, BigDecimal vol) {
        return position.getExitPrice()
                .multiply(position.getExitVol())
                .add(price.multiply(vol))
                .divide(position.getExitVol().add(vol), 10, RoundingMode.DOWN);
    }

}
