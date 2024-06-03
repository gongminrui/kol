package kol.trade.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.common.model.ArrayResponse;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.trade.dto.vo.PositionVo;
import kol.trade.entity.Position;
import kol.trade.repo.InvestmentRepo;
import kol.trade.repo.PositionRepo;
import kol.trade.service.PositionService;
import lombok.AllArgsConstructor;

/**
 * @author kent
 */
@Api(tags = "交易仓位")
@RestController
@RequestMapping("/api/position")
@AllArgsConstructor
public class PositionController {

	final PositionService positionService;
	final PositionRepo positionRepo;
	final InvestmentRepo investmentRepo;

	@ApiOperation("分页查询交易仓位记录")
	@GetMapping("/page")
	@ApiImplicitParams({ @ApiImplicitParam(name = "strategyId", value = "策略ID", dataTypeClass = String.class),
			@ApiImplicitParam(name = "investmentId", value = "跟单ID", dataTypeClass = Long.class),
			@ApiImplicitParam(name = "status", value = "仓位状态", dataTypeClass = String.class),
			@ApiImplicitParam(name = "pageIndex", value = "当前页数", required = true, dataTypeClass = Integer.class),
			@ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataTypeClass = Integer.class) })
	public PageResponse<Position> getPositionPage(
			@RequestParam(value = "investmentId", required = false) Long investmentId,
			@RequestParam(value = "strategyId", required = false) Long strategyId,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
			@RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
		return positionService.getPositionPage(investmentId, strategyId, status, pageIndex, pageSize);
	}

	@ApiOperation("根据日期统计每日盈亏")
	@GetMapping("/cal/profit")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "investmentId", value = "跟单ID", required = true, dataTypeClass = Long.class),
			@ApiImplicitParam(name = "days", value = "天数", required = true, dataTypeClass = Long.class) })
	public ArrayResponse<Map<String, Object>> getPositionProfitByDay(
			@RequestParam(value = "investmentId") Long investmentId,
			@RequestParam(value = "days", defaultValue = "30") Long days) {
		List<Map<String, Object>> result = positionService.getCalPositionProfitByDay(investmentId, days);
		return ArrayResponse.of(result);
	}

	@ApiOperation("统计当日盈亏与开单数量")
	@GetMapping("/cal/today")
	public SingleResponse<Map<String, Object>> getCalTodayPosition() {
		Map<String, Object> result = positionService.getCalTodayPosition();
		return SingleResponse.of(result);
	}

	@ApiOperation("修改仓位信息")
	@PostMapping("/modify")
	public Response modifyStopLossGain(@org.springframework.web.bind.annotation.RequestBody PositionVo positionVo) {
		if(positionVo.getStopLossPrice()!=null&&positionVo.getStopLossPrice().compareTo(BigDecimal.ZERO)==0) {
			positionVo.setStopLossPrice(null);
    	}
    	if(positionVo.getStopGainPrice()!=null&&positionVo.getStopGainPrice().compareTo(BigDecimal.ZERO)==0) {
    		positionVo.setStopGainPrice(null);
    	}
		return positionService.modifyStopLossGain(positionVo);
	}
}
