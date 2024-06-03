package kol.account.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.account.service.AccountRebateService;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.account.dto.cmd.AgentCmd;
import kol.account.dto.vo.AgentVO;
import kol.account.service.AgentService;
import kol.common.model.TimeRange;
import kol.common.utils.TimeRangeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author kent
 */
@Api(tags = "代理模块")
@RestController
@RequestMapping("/api/account/agent")
public class AgentController {
    final AgentService agentService;
    @Resource
    private AccountRebateService accountRebateService;

    public AgentController(AgentService partnerService) {
        this.agentService = partnerService;
    }

    @ApiOperation("添加代理商")
    @PostMapping("/add")
    public Response add(@RequestBody @Valid AgentCmd cmd) {
//        agentService.add(cmd);
        accountRebateService.saveOrUpdate(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("编辑代理商")
    @PostMapping("/modify")
    public Response modify(@RequestBody @Valid AgentCmd cmd) {
//        agentService.modify(cmd);
        accountRebateService.saveOrUpdate(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("代理详情")
    @GetMapping("/info")
    public SingleResponse<AgentVO> info() {
        return SingleResponse.of(accountRebateService.getAgentVO());
    }

    @ApiOperation("获得下级翻页列表")
    @GetMapping("/getNext")
    public PageResponse<AgentVO> getNext(@RequestParam(value = "email", required = false) String email, @RequestParam("pageIndex") int pageIndex,
                                         @RequestParam("pageSize") int pageSize) {
        return accountRebateService.getNext(pageIndex, pageSize, email);
    }

    @ApiOperation("根据email获得下级信息")
    @GetMapping("/getNextByEmail")
    public SingleResponse<AgentVO> getNextByEmail(@RequestParam("email") String email) {
        return SingleResponse.of(accountRebateService.getAgentVOByEmail(email));
    }

    @ApiOperation("测试返佣")
    @GetMapping("/testRebate")
    public Response testRebate(@RequestParam(value = "date", required = false) String date) {
        TimeRange timeRange = StringUtils.isBlank(date) ? TimeRangeUtil.today() : TimeRangeUtil.getTimeRange(date, date);
        accountRebateService.timeRebate(timeRange);
        return Response.buildSuccess();
    }
}
