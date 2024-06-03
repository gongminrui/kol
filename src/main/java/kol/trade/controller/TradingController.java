package kol.trade.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.common.model.BaseDTO;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.common.service.HttpDispenseService;
import kol.common.service.SubscribeService;
import kol.trade.dto.cmd.OrderCmd;
import kol.trade.entity.TraderOrderError;
import kol.trade.service.OrderErrorService;
import kol.trade.service.trading.ClosePositionService;
import kol.trade.service.trading.TradingService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author kent
 */
@Api(tags = "交易模块")
@RestController
@RequestMapping(TradingController.API_URL)
@RequiredArgsConstructor
public class TradingController {
    public static final String API_URL = "/api/trading";
    public static final String MOTHED_POST_ORDER_URL = "/market/postOrder";
    public static final String MOTHED_DISPENSE_ORDER_URL = "/dispense/market/postOrder";

    final TradingService tradingService;
    final ClosePositionService closePositionService;
    final SubscribeService subscribeService;
    final OrderErrorService orderErrorService;

    @Resource
    private HttpDispenseService httpDispenseService;

    @ApiOperation("市价开平仓")
    @PostMapping(MOTHED_POST_ORDER_URL)
    @ResponseBody
    public Response postOrder(@RequestBody @Valid OrderCmd cmd, HttpServletRequest request) {
        tradingService.postOrder(cmd, request);
        return Response.buildSuccess();
    }

    @ApiOperation("后端市价开平仓")
    @PostMapping("/admin/market/postOrder")
    public Response postOrderByAdmin(@RequestBody @Valid OrderCmd cmd, HttpServletRequest request) {
        tradingService.postOrderByAdmin(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("分发服务调用-市价开平仓")
    @PostMapping(MOTHED_DISPENSE_ORDER_URL)
    public Response postOrderByDispense(@RequestBody @Valid OrderCmd cmd) {
        tradingService.postOrderByAdmin(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("市价平仓")
    @PostMapping("/market/close")
    public Response closePosition(@RequestBody @Valid BaseDTO dto) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        closePositionService.closePosition(accountId, dto.getId());
        return Response.buildSuccess();
    }

    @ApiOperation("开平仓错误日志")
    @GetMapping("/orderError/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "账户ID", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "strategyId", value = "策略ID", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataTypeClass = Integer.class)
    })
    public PageResponse<TraderOrderError> listOrderError(@RequestParam(required = false) Long accountId,
                                                         @RequestParam(required = false) Long strategyId,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(defaultValue = "1") Integer pageIndex) {
        return orderErrorService.listOrderErrorToPage(accountId, strategyId, pageSize, pageIndex);
    }
}
