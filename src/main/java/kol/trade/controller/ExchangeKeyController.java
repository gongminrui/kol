package kol.trade.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.ArrayResponse;
import kol.common.model.BaseDTO;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.trade.dto.cmd.ExchangeKeyCmd;
import kol.trade.dto.vo.ExchangeKeyVO;
import kol.trade.service.ExchangeKeyService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author guanzhenggang@gmail.com
 */
@RestController
@Api(tags = "API-KEY模块")
@RequestMapping("/api/exchangeKey")
public class ExchangeKeyController {

    final ExchangeKeyService exchangeKeyService;

    public ExchangeKeyController(ExchangeKeyService exchangeKeyService) {
        this.exchangeKeyService = exchangeKeyService;
    }

    @ApiOperation("添加交易所KEY")
    @PostMapping("/add")
    public Response addKey(@RequestBody @Valid ExchangeKeyCmd cmd) {
        exchangeKeyService.add(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("删除交易所KEY")
    @PostMapping("/delete")
    public Response deleteKey(@RequestBody @Valid BaseDTO dto) {
        exchangeKeyService.delete(dto.getId());
        return Response.buildSuccess();
    }

    @ApiOperation("获取用户Exchange_key")
    @GetMapping("/list")
    public ArrayResponse<ExchangeKeyVO> getExchangeKeyList() {
        List<ExchangeKeyVO> list = exchangeKeyService.listExchangeKey();
        return ArrayResponse.of(list);
    }

    @ApiOperation("测试key")
    @PostMapping("/testKey")
    public Response testKey(@RequestBody @Valid ExchangeKeyCmd cmd) {
        exchangeKeyService.testKey(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("获取交易所KEY当前余额")
    @GetMapping("/balance")
    public SingleResponse getBalance(Long apiKeyId) {
        BigDecimal balance = this.exchangeKeyService.getBalance(apiKeyId);
        return SingleResponse.of(balance);
    }
}
