package kol.trade.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.ArrayResponse;
import kol.common.model.Response;
import kol.trade.entity.Symbol;
import kol.trade.service.SymbolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kent
 */
@Api(tags = "交易对")
@RestController
@RequestMapping("/api/symbol")
public class SymbolController {
    final SymbolService symbolService;

    public SymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    @GetMapping("/list")
    @ApiOperation("可用交易多列表")
    public ArrayResponse<Symbol> getList() {
        List<Symbol> list = symbolService.getList();
        return ArrayResponse.of(list);
    }

    @GetMapping("/refresh")
    public Response refresh() {
        symbolService.refresh();
        return Response.buildSuccess();
    }
}
