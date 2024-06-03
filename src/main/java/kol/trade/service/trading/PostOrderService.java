package kol.trade.service.trading;

import io.contek.invoker.okx.api.common._Order;
import kol.common.utils.JsonUtils;
import kol.exchange.service.OkxService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.ExchangeKey;
import kol.trade.entity.Investment;
import kol.trade.entity.TradeOrder;
import kol.trade.repo.TradeOrderRepo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 交易所开平单，并保存数据
 *
 * @author kent
 */
@Service
public class PostOrderService {

    final OkxService exchangeUtil;
    final TradeOrderRepo tradeOrderRepo;

    public PostOrderService(OkxService exchangeUtil, TradeOrderRepo tradeOrderRepo) {
        this.exchangeUtil = exchangeUtil;
        this.tradeOrderRepo = tradeOrderRepo;
    }

    public TradeOrder postOrder(Investment invest, OrderCmd cmd, BigDecimal vol) {
        ExchangeKey exchangeKey = JsonUtils.jsonToPojo(invest.getApiKey(), ExchangeKey.class);
        _Order order = exchangeUtil.postOrder(exchangeKey, cmd, vol);
        return saveTradeOrder(order, invest, cmd);
    }

    private TradeOrder saveTradeOrder(_Order order, Investment invest, OrderCmd cmd) {
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
        tradeOrder.setAvgPrice(new BigDecimal(order.avgPx));
        tradeOrder.setVol(new BigDecimal(order.accFillSz));
        BigDecimal fee = new BigDecimal(order.fee).abs();
        tradeOrder.setFee(fee);
        tradeOrder.setExchangeOrderId(order.ordId);
        tradeOrder.setIsReal(invest.getIsReal());
        return tradeOrderRepo.save(tradeOrder);
    }
}
