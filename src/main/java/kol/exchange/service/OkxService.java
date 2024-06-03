package kol.exchange.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import io.contek.invoker.commons.rest.RestErrorException;
import kol.common.annotation.NotLogRecord;
import kol.common.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.contek.invoker.commons.ApiContext;
import io.contek.invoker.commons.rest.RestContext;
import io.contek.invoker.commons.websocket.WebSocketContext;
import io.contek.invoker.okx.api.ApiFactory;
import io.contek.invoker.okx.api.common._AccountBalance;
import io.contek.invoker.okx.api.common._AccountConfig;
import io.contek.invoker.okx.api.common._Instrument;
import io.contek.invoker.okx.api.common._Order;
import io.contek.invoker.okx.api.common._Position;
import io.contek.invoker.okx.api.common._Ticker;
import io.contek.invoker.okx.api.rest.market.GetMarketTicker;
import io.contek.invoker.okx.api.rest.market.GetPublicInstruments;
import io.contek.invoker.okx.api.rest.market.MarketRestApi;
import io.contek.invoker.okx.api.rest.user.GetAccountBalance;
import io.contek.invoker.okx.api.rest.user.GetAccountPositions;
import io.contek.invoker.okx.api.rest.user.GetTradeOrder;
import io.contek.invoker.okx.api.rest.user.PostTradeOrder;
import io.contek.invoker.okx.api.rest.user.UserRestApi;
import io.contek.invoker.security.ApiKey;
import io.github.itning.retry.Retryer;
import io.github.itning.retry.RetryerBuilder;
import io.github.itning.retry.strategy.stop.StopStrategies;
import io.github.itning.retry.strategy.wait.WaitStrategies;
import kol.common.config.GlobalConfig;
import kol.common.utils.NetKits;
import kol.config.model.Config.KeyEnum;
import kol.config.service.ConfigService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.ExchangeKey;
import kol.trade.entity.Symbol;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.spring.web.json.Json;

/**
 * okx交易所工具类
 *
 * @author kent
 */
@Slf4j
@Service
@Order(1)
public class OkxService {
    /**
     * 交易所接口执行成功
     */
    private static final String SUCCESS = "0";
    /**
     * 交易对最新价格
     */
    private Map<String, String> symbolPriceMap = new ConcurrentHashMap<>();
    /**
     * 交易对基本信息
     */
    private Map<String, _Instrument> instrumentMap = new ConcurrentHashMap<>();
    /**
     * 交易所基础信息
     */
    private ApiContext context;
    /**
     * API实例化工厂
     */
    private ApiFactory apiFactory;
    /**
     * 交易所行情API
     */
    private MarketRestApi marketRestApi;

    final GlobalConfig config;
    @Autowired
    private ConfigService configService;

    public OkxService(GlobalConfig config) {
        if (config.getUseProxy()) {
            NetKits.setProxyNet();
        }
        this.config = config;
        this.context = ApiContext.newBuilder()
                .setRestContext(RestContext.forBaseUrl("https://www.okx.com", config.getSimulated()))
                .setWebSocketContext(WebSocketContext.forBaseUrl("wss://ws.okx.com:8443", Duration.ofSeconds(5), config.getSimulated()))
                .build();
        if (this.config.getSimulated()) {
            log.info("使用交易所模拟环境");
            this.apiFactory = ApiFactory.fromContext(context);
        } else {
            log.info("使用实盘环境");
            this.apiFactory = ApiFactory.getMainNet();
        }
    }

    /**
     * 获取用户交易所API
     *
     * @param exchangeKey
     * @return
     */
    private UserRestApi getUserApi(ExchangeKey exchangeKey) {
        ApiKey apiKey = buildApiKey(exchangeKey);
        return apiFactory.rest().user(apiKey);
    }

    private MarketRestApi getMarketRestApi(ExchangeKey exchangeKey) {
        if (Objects.isNull(this.marketRestApi)) {
            this.marketRestApi = this.apiFactory.rest().market();
        }
        return this.marketRestApi;
    }

    public ApiKey buildApiKey(ExchangeKey exchangeKey) {
        if (Objects.isNull(exchangeKey)) {
            exchangeKey = new ExchangeKey();
            exchangeKey.setApiKey(config.getKey());
            exchangeKey.setSecretKey(config.getSecret());
            exchangeKey.setPassphrase(config.getPassword());
        }
        ApiKey.Builder builder = ApiKey.newBuilder()
                .setId(exchangeKey.getApiKey())
                .setSecret(exchangeKey.getSecretKey())
                .addProperty("OK-ACCESS-PASSPHRASE", exchangeKey.getPassphrase());
        return builder.build();
    }

    /**
     * 执行开平仓
     *
     * @param exchangeKey
     * @param cmd
     * @param vol         标的物成交量
     * @return
     */
    public _Order postOrder(ExchangeKey exchangeKey, OrderCmd cmd, BigDecimal vol) {
        _Instrument instrument = this.instrumentMap.get(cmd.getSymbol());
        Integer sz = null;
        if (instrument.settleCcy.equals(instrument.ctValCcy)) {
            sz = vol.multiply(getPrice(cmd.getSymbol())).divide(new BigDecimal(instrument.ctVal), 0, RoundingMode.DOWN).intValue();
        } else {
            sz = vol.divide(new BigDecimal(instrument.ctVal), 0, RoundingMode.DOWN).intValue();
        }
        return postOrder(exchangeKey, cmd, sz);
    }

    /**
     * @param exchangeKey
     * @param cmd
     * @param sz          张数
     * @return
     */
    public _Order postOrder(ExchangeKey exchangeKey, OrderCmd cmd, Integer sz) {
        _Instrument instrument = this.instrumentMap.get(cmd.getSymbol());
        Assert.isTrue(sz >= 1, "小于交易所最小开仓量，无法开仓。");
        UserRestApi userRestApi = this.getUserApi(exchangeKey);
        PostTradeOrder.Response res = userRestApi.postTradeOrder()
                .setInstId(cmd.getSymbol())
                .setTdMode("cross")
                .setOrdType("market")
                .setSide(cmd.getTradeType().getSide().toString().toLowerCase())
                .setPosSide(cmd.getTradeType().getPositionSide().toString().toLowerCase())
                .setSz(sz.toString())
                .setTag("cef64854fbe8BCDE")
                .submit();

        _Order order = null;
        if (SUCCESS.equals(res.code) && !CollectionUtils.isEmpty(res.data)) {
            order = getOrder(userRestApi, res.data.get(0).ordId, cmd.getSymbol());
            this.checkOrder(order, cmd, sz);
            order.accFillSz = new BigDecimal(order.accFillSz)
                    .multiply(new BigDecimal(instrument.ctVal)).toString();
        } else {
            Assert.isTrue(false, res.msg);
        }
        return order;
    }

    /**
     * 处理平仓数量与请求数量不一直情况
     *
     * @param order
     * @param cmd
     * @param sz
     */
    private void checkOrder(_Order order, OrderCmd cmd, Integer sz) {
        if (sz > Integer.parseInt(order.accFillSz)
                && cmd.getTradeType().toString().toUpperCase().contains("CLOSE")) {
            BigDecimal fee = new BigDecimal(order.fee);
            BigDecimal accFillVol = new BigDecimal(order.accFillSz);
            BigDecimal vol = new BigDecimal(sz.toString());
            fee = fee.divide(accFillVol, 4, RoundingMode.DOWN).multiply(vol);

            order.accFillSz = vol.toString();
            order.fee = fee.toString();
        }
    }

    /**
     * 获取交易所订单详情
     *
     * @param userRestApi
     * @param ordId
     * @return
     */
    private _Order getOrder(UserRestApi userRestApi, String ordId, String symbol) {
        _Order order = new _Order();
        Retryer<_Order> retry = RetryerBuilder.<_Order>newBuilder()
                .withStopStrategy(StopStrategies.stopAfterAttempt(5))
                .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
                .retryIfRuntimeException()
                .build();
        try {
            order = retry.call(() -> {
                GetTradeOrder.Response res = userRestApi.getTradeOrder()
                        .setInstId(symbol)
                        .setOrdId(ordId).submit();
                if (SUCCESS.equals(res.code)
                        && !CollectionUtils.isEmpty(res.data)
                        && "filled".equals(res.data.get(0).state)) {
                    return res.data.get(0);
                } else {
                    throw new RuntimeException("查询订单信息失败");
                }
            });
        } catch (Exception ex) {
            log.error("getOrder：", ex);
        }
        return order;
    }

    /**
     * 加载交易所合约所有交易对基础信息
     *
     * @return
     */
    public void loadInstruments() {
        this.getMarketRestApi(null);
        GetPublicInstruments.Response res = this.marketRestApi.getPublicInstruments()
                .setInstType("SWAP").submit();
        if (SUCCESS.equals(res.code) && !CollectionUtils.isEmpty(res.data)) {
            List<_Instrument> instrumentList = res.data;
            instrumentMap = instrumentList.stream()
                    .collect(Collectors.toMap(instrument -> instrument.instId, instrument -> instrument));
        }
    }

    /**
     * 查询用户余额
     *
     * @param exchangeKey
     * @return
     */
    public _AccountBalance getAccountBalance(ExchangeKey exchangeKey) {
        UserRestApi userRestApi = getUserApi(exchangeKey);
        try {
            GetAccountBalance.Response res = userRestApi.getAccountBalance().submit();
            _AccountBalance balance = null;
            if (SUCCESS.equals(res.code) && !CollectionUtils.isEmpty(res.data)) {
                balance = res.data.get(0);
            }
            return balance;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() + " key=" + exchangeKey.getApiKey(), e);
        }
    }

    /**
     * 获取交易对最新价格
     *
     * @param symbol
     * @return
     */
    public _Ticker getTicker(String symbol) {
        this.getMarketRestApi(null);
        GetMarketTicker.Response res = this.marketRestApi.getMarketTicker().setInstId("SWAP").setInstId(symbol).submit();
        _Ticker ticker = null;
        if (SUCCESS.equals(res.code) && !CollectionUtils.isEmpty(res.data)) {
            ticker = res.data.get(0);
        }
        return ticker;
    }

    /**
     * 获取最新价格
     *
     * @param symbol
     * @return
     */
    public BigDecimal getPrice(String symbol) {
        String price = symbolPriceMap.get(symbol);
        if (StringUtils.isBlank(price)) {
            _Ticker ticker = getTicker(symbol);
            price = ticker.last;
        }
        return new BigDecimal(price);
    }

    /**
     * 设置交易对最新价格
     *
     * @param symbol
     * @param price
     */
    @NotLogRecord
    public void setPrice(String symbol, String price) {
        symbolPriceMap.put(symbol, price);
    }

    /**
     * 设置双向持仓与全仓杠杆
     *
     * @param exchangeKey
     * @param lever
     */
    public void postSetAccount(ExchangeKey exchangeKey, String lever, List<Symbol> symbolList) {
        UserRestApi userRestApi = this.getUserApi(exchangeKey);
        try {
            _Position position = null;
            GetAccountPositions.Response res = userRestApi.getAccountPositions().submit();
            if (SUCCESS.equals(res.code) && !CollectionUtils.isEmpty(res.data)) {
                position = res.data.get(0);
            }
            Assert.isTrue(Objects.isNull(position), "当前有持仓，请平仓后添加。");
        } catch (RestErrorException ex) {
            JsonNode rootNode = JsonUtils.getJsonNode(ex.getMessage());
            String code = rootNode.get("code").asText();
            if ("50105".equals(code) || "50104".equals(code)) {
                Assert.isTrue(false, "你输入的密码与交易所创建API时输入的不一致");
            } else {
                log.error("postSetAccount：{}", ex);
                Assert.isTrue(false, "使用API查询交易所账户信息出错");
            }
        }

        _AccountConfig config = userRestApi.getAccountConfig().submit().data.get(0);
        String ip = configService.getValue(KeyEnum.SERVER_IP);
        if (ip == null) {
            ip = "16.162.79.61";
            configService.set(KeyEnum.SERVER_IP, ip, "服务器IP");
        }

        boolean result = config.acctLv.equals("2");
        Assert.isTrue(result, "交易所账户必须设置为单币种保证金模式");

        String[] ips = ip.split(",");
        for (String str : ips) {
//            result = config.ip.contains(str);
        }
        Assert.isTrue(result, "APIKEY需要请绑定IP=" + ip);


        //TODO 查询用户配置信息并判断配置是否符合要求。

        try {
            userRestApi.postAccountSetPositionMode().setPosMode("long_short_mode").submit();
            for (Symbol symbol : symbolList) {
                userRestApi.postAccountSetLeverage()
                        .setInstId(symbol.getSymbolTitle())
                        .setLever(lever)
                        .setMgnMode("cross")
                        .submit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("设置杠杆失败: ", e);
        }

    }

    public List<_Position> getCurrentPosition(ExchangeKey exchangeKey, String instId) {
        UserRestApi userRestApi = this.getUserApi(exchangeKey);
        return userRestApi.getAccountPositions().setInstId(instId).setInstType("SWAP").submit().data;
    }

    public Integer volumeToSz(BigDecimal vol, String symbol) {
        _Instrument instrument = this.instrumentMap.get(symbol);
        Integer sz = null;
        if (instrument.settleCcy.equals(instrument.ctValCcy)) {
            sz = vol.multiply(getPrice(symbol)).divide(new BigDecimal(instrument.ctVal), 0, RoundingMode.DOWN).intValue();
        } else {
            sz = vol.divide(new BigDecimal(instrument.ctVal), 0, RoundingMode.DOWN).intValue();
        }
        return sz;
    }

    public BigDecimal szToVolume(Integer sz, String symbol) {
        _Instrument instrument = this.instrumentMap.get(symbol);
        return BigDecimal.valueOf(sz).multiply(new BigDecimal(instrument.ctVal));
    }

    @NotLogRecord
    public Map<String, _Instrument> getInstrumentMap() {
        return instrumentMap;
    }

    @NotLogRecord
    public ApiFactory getApiFactory() {
        return apiFactory;
    }
}
