package kol.trade.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.common.model.ArrayResponse;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.trade.dto.cmd.StrategyCmd;
import kol.trade.dto.vo.StrategyVo;
import kol.trade.entity.Strategy;
import kol.trade.service.StrategyService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author guan
 */
@RestController
@Api(tags = "策略模块")
@RequestMapping("/api/strategy")
public class StrategyController {

    final StrategyService strategyService;

    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @ApiOperation("策略列表")
    @GetMapping("/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "strategyName", value = "策略名称", dataTypeClass = String.class),
            @ApiImplicitParam(name = "tags", value = "标签，例如：'高频%稳定%日内'", dataTypeClass = String.class),
            @ApiImplicitParam(name = "status", value = "策略状态", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataTypeClass = Integer.class)
    })
    public ArrayResponse<StrategyVo> list(@RequestParam(value = "strategyName", required = false) String strategyName,
                                          @RequestParam(value = "tags", required = false) String tags,
                                          @RequestParam(value = "type", required = false) Integer type,
                                          @RequestParam(value = "status", required = false, defaultValue = "ON") String status,
                                          @RequestParam(value = "pageIndex", defaultValue = "1", required = false) Integer pageIndex,
                                          @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        List<StrategyVo> strategyList = strategyService.list(strategyName, null, tags, status, type, pageIndex, pageSize);
        return ArrayResponse.of(strategyList);
    }

    @ApiOperation("策略列表-后端")
    @GetMapping("/admin/list")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "strategyName", value = "策略名称", dataTypeClass = String.class),
            @ApiImplicitParam(name = "tags", value = "标签，例如：'高频%稳定%日内'", dataTypeClass = String.class),
            @ApiImplicitParam(name = "status", value = "策略状态", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataTypeClass = Integer.class)
    })
    public ArrayResponse<StrategyVo> listAdmin(@RequestParam(value = "strategyName", required = false) String strategyName,
                                               @RequestParam(value = "tags", required = false) String tags,
                                               @RequestParam(value = "type", required = false) Integer type,
                                               @RequestParam(value = "status", required = false, defaultValue = "ON") String status,
                                               @RequestParam(value = "pageIndex", defaultValue = "1", required = false) Integer pageIndex,
                                               @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<StrategyVo> strategyList = strategyService.list(strategyName, accountId, tags, status, type, pageIndex, pageSize);
        return ArrayResponse.of(strategyList);
    }


    @ApiOperation("根据ID查询策略详情")
    @GetMapping("/get/single")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "strategyId", value = "策略ID", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "num", value = "策略收益率数据数量", required = false, dataTypeClass = Integer.class)
    })
    public SingleResponse<StrategyVo> getSingle(@RequestParam(value = "strategyId") Long strategyId,
                                              @RequestParam(value = "num", required = false, defaultValue = "100") Integer num) {
        StrategyVo strategy = strategyService.getById(strategyId, num);
        return SingleResponse.of(strategy);
    }

    @ApiOperation("策略盈利曲线数据")
    @GetMapping("/list/profitRate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "strategyId", value = "策略ID", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "num", value = "策略收益率数据数量", required = false, dataTypeClass = Integer.class)
    })
    public ArrayResponse<BigDecimal> listToProfitRate(@RequestParam(value = "strategyId") Long strategyId,
                                                      @RequestParam(value = "num", required = false, defaultValue = "100") Integer num) {
        List<BigDecimal> profitRateList = strategyService.getProfitRateList(strategyId, num);
        return ArrayResponse.of(profitRateList);
    }

    @ApiOperation("添加策略")
    @PostMapping("/add")
    public SingleResponse<String> add(@RequestBody @Valid StrategyCmd cmd) {
        String secretKey = strategyService.add(cmd);
        return SingleResponse.of(secretKey);
    }

    @ApiOperation("编辑策略")
    @PostMapping("/edit")
    public Response modify(@RequestBody @Valid StrategyCmd cmd) {
        strategyService.modify(cmd);
        return Response.buildSuccess();
    }

}
