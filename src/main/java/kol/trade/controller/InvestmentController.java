package kol.trade.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import kol.common.model.ArrayResponse;
import kol.common.model.BaseDTO;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.trade.dto.cmd.InvestmentCmd;
import kol.trade.dto.cmd.PageInvestmentCmd;
import kol.trade.entity.Investment;
import kol.trade.service.InvestmentService;

/**
 * @author kent
 */
@Api(tags = "策略跟投")
@RestController
@RequestMapping("/api/investment")
public class InvestmentController {

    final InvestmentService investService;

    public InvestmentController(InvestmentService investService) {
        this.investService = investService;
    }

    @ApiOperation("添加跟投")
    @PostMapping("/add")
    public Response add(@RequestBody @Valid InvestmentCmd cmd) {
        investService.add(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("停止跟投")
    @PostMapping("/stop")
    public Response stop(@RequestBody @Valid BaseDTO dto) {
        investService.stopInvestment(dto.getId());
        return Response.buildSuccess();
    }

    @ApiOperation("获取跟投详情")
    @GetMapping("/info")
    @ApiImplicitParam(name = "investmentId", value = "跟投ID", required = true, dataTypeClass = Long.class)
    public SingleResponse<Investment> getInfo(@RequestParam(value = "investmentId") Long investmentId) {
        Investment investment = investService.getInfo(investmentId);
        return SingleResponse.of(investment);
    }

    @ApiOperation("获取用户历史跟投")
    @GetMapping("/list/history")
    public ArrayResponse<Map<String, Object>> listHistory() {
        List<Map<String, Object>> list = investService.listHistory();
        return ArrayResponse.of(list);
    }

    @ApiOperation("获取用户当前跟投")
    @GetMapping("/list/current")
    public ArrayResponse<Map<String, Object>> listCurrent() {
        List<Map<String, Object>> list = investService.listCurrent();
        return ArrayResponse.of(list);
    }

    @ApiOperation("根据策略获得跟单列表")
    @GetMapping("/pageInvestment")
    public PageResponse<Investment> pageInvestment(@RequestParam("strategyId") Long strategyId,
                                                   @RequestParam("email") String email,
                                                   @RequestParam("pageIndex") int pageIndex,
                                                   @RequestParam("pageSize") int pageSize) {
        PageInvestmentCmd pageInvestmentCmd = new PageInvestmentCmd();
        pageInvestmentCmd.setStrategyId(strategyId);
        pageInvestmentCmd.setEmail(email);
        pageInvestmentCmd.setPageIndex(pageIndex);
        pageInvestmentCmd.setPageSize(pageSize);
        return investService.pageInvestment(pageInvestmentCmd);
    }

}
