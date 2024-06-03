package kol.trade.service.trading;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kol.common.utils.JsonUtils;
import kol.common.utils.SignUtils;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.repo.StrategyDispatchRepo;
import kol.trade.service.PositionService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StrategyDispatchService {
    @Autowired
    StrategyDispatchRepo repo;
    @Autowired
    PositionService positionService;

    HttpClient client = HttpClient.newHttpClient();

    public void dispatch(OrderCmd cmd) {
        OrderCmd params = new OrderCmd();
        BeanUtils.copyProperties(cmd, params);
        params.setStopLossPrice(BigDecimal.ZERO);
        params.setStopGainPrice(BigDecimal.ZERO);
        repo.findByStrategyId(cmd.getStrategyId()).forEach(item -> {
            try {
                Long time = System.currentTimeMillis();
                String raw = time.toString() + item.getDispatchStrategyId() + cmd.getSymbol();
                String sign = SignUtils.sign(raw, item.getApiSecret());
                params.setStrategyId(item.getDispatchStrategyId());
                HttpRequest request = HttpRequest.newBuilder().uri(new URI(item.getUrl()))
                        .header("ACCESS-SIGN", sign)
                        .header("ACCESS-TIMESTAMP", time + "")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.objectToJson(params)))
                        .timeout(Duration.ofSeconds(5)).build();
                HttpResponse<String> body = client.send(request, BodyHandlers.ofString());
                log.info("{}", body.body());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        });

    }
}
