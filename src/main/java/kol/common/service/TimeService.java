package kol.common.service;

import kol.account.service.AccountRebateService;
import kol.common.cache.GlobalCache;
import kol.common.config.GlobalConfig;
import kol.common.model.MessageType;
import kol.common.model.TimeRange;
import kol.common.utils.TimeRangeUtil;
import kol.money.model.Wallet;
import kol.money.repo.WalletRepo;
import kol.money.service.WalletService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.Investment;
import kol.trade.entity.Position;
import kol.trade.enums.CapitalModeEnum;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.TradeTypeEnum;
import kol.trade.repo.InvestmentRepo;
import kol.trade.service.trading.ClosePositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时服务
 *
 * @author Gongminrui
 * @date 2023-03-20 16:20
 */
@Service
@Slf4j
public class TimeService extends BaseService {

    @Resource
    private AccountRebateService accountRebateService;
    @Resource
    private WalletService walletService;
    @Resource
    private ClosePositionService closePositionService;
    @Resource
    private InvestmentRepo investmentRepo;
    @Resource
    private WalletRepo walletRepo;
    @Resource
    private GlobalCache globalCache;
    @Resource
    private GlobalConfig globalConfig;

    /**
     * 定时返佣
     */
    @Scheduled(cron = "${rebate.cron}")
    public void timeRebate() {
        TimeRange timeRange = TimeRangeUtil.yestertoday();
        accountRebateService.timeRebate(timeRange);
    }

    /**
     * 定时检查用户持仓所需要信用金是否充足
     */
    @Scheduled(fixedDelay = 60000)
    public void realTimeCalPrice() {
        if (!globalConfig.isRealTimeMode()) {
            return;
        }
        handlePrice();
    }

    private void handlePrice() {
        if (globalCache.positionMap.isEmpty()) {
            return;
        }
        Map<Long, List<Position>> accPositionMap = this.getPosition(globalCache.positionMap);

        List<Long> accountIds = accPositionMap.keySet().stream().toList();

        Map<Long, BigDecimal> walletMap = walletRepo.findByAccountIdIn(accountIds)
                .parallelStream().collect(Collectors.toMap(Wallet::getAccountId, Wallet::getBalance));


        List<Long> investIds = globalCache.positionMap.values().parallelStream().map(Position::getInvestmentId).toList();

        Map<Long, Investment> investMap = investmentRepo.findByIdIn(investIds)
                .parallelStream().collect(Collectors.toMap(Investment::getId, invest -> invest));

        accPositionMap.keySet().parallelStream().forEach(accId -> {
            List<Position> positions = accPositionMap.get(accId);
            this.calPosition(accId, positions, walletMap.get(accId), investMap);
        });
    }

    private Map<Long, List<Position>> getPosition(Map<String, Position> map) {
        Map<Long, List<Position>> positionMap = map.values().parallelStream()
                .filter(p -> p.getIsReal().equals(Boolean.TRUE))
                .collect(Collectors.groupingBy(Position::getAccountId));
        return positionMap;
    }

    /**
     * 需要要平仓的跟单
     *
     * @param accountId
     * @param positions
     */
    private void calPosition(Long accountId, List<Position> positions, BigDecimal balance, Map<Long, Investment> investMap) {
        if (CollectionUtils.isEmpty(positions)) {
            return;
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Position position : positions) {
            totalAmount = position.getFollowCost().add(totalAmount);
        }

        if (balance.multiply(BigDecimal.valueOf(0.99)).compareTo(totalAmount) > 0) {
            return;
        }

        for (Position position : positions) {
            OrderCmd orderCmd = this.buildOrderCmd(position);
            Investment invest = investMap.get(position.getInvestmentId());
            // 强制平仓
            closePositionService.execute(orderCmd, invest, position);

            MessageType messageType = MessageType.EARNEST_MONEY_DEFICIENCY;
            investmentRepo.endInvestment(invest.getId(), messageType.getMsg());
            // 发送消息
            sendMessage(accountId, messageType, messageType);

        }
    }

    /**
     * 构建开仓仓信息
     *
     * @param position
     * @return
     */
    private OrderCmd buildOrderCmd(Position position) {
        OrderCmd orderCmd = new OrderCmd();
        orderCmd.setStrategyId(position.getStrategyId());
        orderCmd.setSymbol(position.getSymbol());
        orderCmd.setLeverage(position.getLeverage());
        orderCmd.setMarket(position.getMarket());
        orderCmd.setPositionNo(position.getPositionNo());
        orderCmd.setExchange(position.getExchange());
        TradeTypeEnum tradeType = PositionSideEnum.LONG.equals(position.getPositionSide())
                ? TradeTypeEnum.CLOSE_LONG : TradeTypeEnum.CLOSE_SHORT;
        orderCmd.setTradeType(tradeType);
        orderCmd.setPositionRatio(BigDecimal.ONE);
        orderCmd.setCapitalMode(CapitalModeEnum.REMAIND);
        return orderCmd;
    }

}
