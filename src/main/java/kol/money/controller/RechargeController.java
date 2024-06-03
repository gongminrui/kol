package kol.money.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.Response;
import kol.money.service.RechargeService;
@Api(tags = "充值")
@RestController
public class RechargeController {

	@Autowired
	private RechargeService rechargeService;
	
	@GetMapping("/api/recharge-txid")
	@ApiOperation("充值")
	public Response recharge(@RequestParam String txid) {
		rechargeService.recharge(txid);
		return Response.buildSuccess();
	}
	
	@GetMapping("/api/recharge-hours")
	@ApiOperation("充值")
	public Response recharge(@RequestParam int hours) {
		Duration duration=Duration.ofHours(hours);
		rechargeService.recharge(duration);
		return Response.buildSuccess();
	}
}
