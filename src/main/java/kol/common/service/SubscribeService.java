package kol.common.service;

import com.google.common.eventbus.AsyncEventBus;
import io.contek.invoker.commons.websocket.ConsumerState;
import io.contek.invoker.commons.websocket.ISubscribingConsumer;
import io.contek.invoker.commons.websocket.SubscriptionState;
import io.contek.invoker.okx.api.ApiFactory;
import io.contek.invoker.okx.api.common._Ticker;
import io.contek.invoker.okx.api.websocket.market.MarketWebSocketApi;
import io.contek.invoker.okx.api.websocket.market.TickersChannel;
import kol.common.annotation.NotLogRecord;
import kol.exchange.service.OkxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * socket订阅服务
 *
 * @author kent
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscribeService {
    private static Map<String, String> symbolMap = new ConcurrentHashMap<>();

    final AsyncEventBus asyncEventBus;
    final OkxService exchangeUtil;
    private MarketWebSocketApi marketSocket;

    public void init() {
        exchangeUtil.loadInstruments();
    }

    /**
     * 订阅交易对Ticker数据
     *
     * @param symbol
     */
    @NotLogRecord
    public void subscribeTicker(String symbol) {
        if (symbolMap.containsKey(symbol)) {
            return;
        }
        symbolMap.put(symbol, symbol);
        if (marketSocket == null) {
            ApiFactory apiFactory = exchangeUtil.getApiFactory();
            this.marketSocket = apiFactory.ws().market();
        }

        marketSocket.getTickerChannel(symbol).addConsumer(new ISubscribingConsumer<>() {
            @Override
            public void onStateChange(SubscriptionState subscriptionState) {
                if (SubscriptionState.SUBSCRIBED.equals(subscriptionState)) {
                    log.info("已订阅{}行情数据", symbol);
                }
            }

            @Override
            public void onNext(TickersChannel.Message message) {
                _Ticker ticker = message.data.get(0);
                exchangeUtil.setPrice(ticker.instId, ticker.last);
                asyncEventBus.post(ticker);
            }

            @Override
            public ConsumerState getState() {
                return ConsumerState.ACTIVE;
            }
        });

    }
}
