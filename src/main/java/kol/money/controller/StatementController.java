package kol.money.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.common.model.BaseDTO;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.money.model.Statement;
import kol.money.service.StatementService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author kent
 */
@Api(tags = "结账单接口")
@RestController
@RequestMapping("/api/statement")
public class StatementController {
    @Resource
    private StatementService statementService;

    @GetMapping("pageMyStatement")
    @ApiOperation("app-获得我的翻页账单数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataTypeClass = Integer.class)
    })
    public PageResponse<Statement> pageMyStatement(
            @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return statementService.pageMyStatement(pageIndex, pageSize);
    }

    @GetMapping("pageStatement")
    @ApiOperation("管理后台-获得账单翻页数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "email", value = "邮箱", required = false, dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataTypeClass = Integer.class)
    })
    public PageResponse<Statement> pageStatement(@RequestParam(value = "email", required = false) String email,
                                                 @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                 @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return statementService.pageStatement(null, email, pageIndex, pageSize);
    }

    @PostMapping("settle")
    @ApiOperation("用户结账")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "结算账单id", required = true, dataTypeClass = String.class),
    })
    public Response settle(@RequestBody @Validated BaseDTO baseDTO) {
        statementService.settle(baseDTO.getId());
        return Response.buildSuccess();
    }

    @PostMapping("manualGenStatement")
    @ApiOperation("手动生成账单")
    public Response manualGenStatement() {
        statementService.manualGenStatement();
        return Response.buildSuccess();
    }

    @PostMapping("handleNotSettleStatement")
    @ApiOperation("手动结算账单，模拟1号自动结算账单功能")
    public Response handleNotSettleStatement() {
        statementService.handleNotSettleStatement();
        return Response.buildSuccess();
    }

}
