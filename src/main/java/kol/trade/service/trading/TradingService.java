package kol.trade.service.trading;

import kol.common.service.SubscribeService;
import kol.common.utils.SignUtils;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.Strategy;
import kol.trade.enums.TradeTypeEnum;
import kol.trade.repo.StrategyRepo;
import kol.trade.service.SymbolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * 策略交易
 *
 * @author kent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradingService {

    final StrategyRepo strategyRepo;
    final SubscribeService subscribeService;
    final OpenPositionService openPositionService;
    final ClosePositionService closePositionService;
    final SymbolService symbolService;

    /**
     * 开单（多空）
     */
    public void postOrder(OrderCmd cmd, HttpServletRequest request) {
        Strategy strategy = verify(cmd, request);
        subscribeService.subscribeTicker(cmd.getSymbol());
        cmd.setExchange(strategy.getExchange());
        if (TradeTypeEnum.OPEN_LONG.equals(cmd.getTradeType())
                || TradeTypeEnum.OPEN_SHORT.equals(cmd.getTradeType())) {
            Long positionNo = System.currentTimeMillis();
            cmd.setPositionNo(positionNo);
            openPositionService.openPosition(cmd);
        } else {
            closePositionService.closePosition(cmd);
        }
    }

    /**
     * 开单（多空）
     */
    public void postOrderByAdmin(OrderCmd cmd) {
        Strategy strategy = verifyByAdmin(cmd);
        subscribeService.subscribeTicker(cmd.getSymbol());
        cmd.setExchange(strategy.getExchange());
        if (TradeTypeEnum.OPEN_LONG.equals(cmd.getTradeType())
                || TradeTypeEnum.OPEN_SHORT.equals(cmd.getTradeType())) {
            Long positionNo = System.currentTimeMillis();
            cmd.setPositionNo(positionNo);
            openPositionService.openPosition(cmd);
        } else {
            closePositionService.closePosition(cmd);
        }
    }

    /**
     * 验证策略与开仓信号
     *
     * @param cmd
     * @return
     */
    private Strategy verify(OrderCmd cmd, HttpServletRequest request) {
        String sign = request.getHeader("ACCESS-SIGN");
        boolean result = StringUtils.isNotBlank(sign);
        Assert.isTrue(result, "缺少签名");

        String timestamp = request.getHeader("ACCESS-TIMESTAMP");
        result = StringUtils.isNotBlank(timestamp);
        Assert.isTrue(result, "缺少发起请求时间戳");

        Long requestTime = Long.parseLong(timestamp) + 10000L;
        Long nowTime = System.currentTimeMillis();
        result = requestTime >= nowTime;
        Assert.isTrue(result, "请求超时，请重试！");

        Optional<Strategy> optional = strategyRepo.findById(cmd.getStrategyId());
        Assert.isTrue(optional.isPresent(), "策略不存在");
        Strategy strategy = optional.get();
        result = Strategy.StatusEnum.ON.equals(strategy.getStatus());
        Assert.isTrue(result, "策略已关闭，无法开仓");

        String data = timestamp + cmd.getStrategyId() + cmd.getSymbol();
        result = SignUtils.verify(data, strategy.getSecretKey(), sign);
        Assert.isTrue(result, "验签失败");

        result = symbolService.checkSymbol(cmd.getSymbol());
        Assert.isTrue(result, "交易对非法，不在平台允许范围内");

        return strategy;


    }

    /**
     * 后端服务平台开单
     *
     * @param cmd
     * @return
     */
    private Strategy verifyByAdmin(OrderCmd cmd) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Strategy> optional = strategyRepo.findByAccountIdAndId(accountId, cmd.getStrategyId());
        Assert.isTrue(optional.isPresent(), "策略不存在或没有策略操作权限");
        Strategy strategy = optional.get();
        boolean result = Strategy.StatusEnum.ON.equals(strategy.getStatus());
        Assert.isTrue(result, "策略已关闭，无法开仓");

        result = symbolService.checkSymbol(cmd.getSymbol());
        Assert.isTrue(result, "交易对非法，不在平台允许范围内");
        return strategy;
    }
}
