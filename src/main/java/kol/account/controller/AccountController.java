package kol.account.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import kol.account.dto.cmd.AddAccountCmd;
import kol.account.dto.cmd.RegisterCmd;
import kol.account.dto.cmd.UpdateStatusCmd;
import kol.account.dto.vo.AccountRebateVo;
import kol.account.dto.vo.AccountVO;
import kol.account.service.AccountRelationService;
import kol.account.service.AccountService;
import kol.account.service.RegisterService;
import kol.common.model.ArrayResponse;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.money.service.RebateService;
import kol.money.service.WalletService;
import kol.vip.service.VipService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * @author guan
 */
@RestController
@Api(tags = "账户模块")
@RequestMapping("/api/account")
public class AccountController {
    final RegisterService registerService;
    final AccountService accountService;
    @Resource
    AccountRelationService accountRelationService;
    @Resource
    private VipService vipService;
    @Resource
    private RebateService rebateService;
    @Resource
    private WalletService walletService;

    public AccountController(RegisterService registerService, AccountService accountService) {
        this.registerService = registerService;
        this.accountService = accountService;
    }

    @GetMapping("/me")
    @ApiOperation("我的个人信息")
    public SingleResponse<AccountVO> me(HttpServletRequest request) {
        AccountVO vo = accountService.me(request);
        return SingleResponse.of(vo);
    }

    @ApiOperation("注册，注册前请调用验证码发送接口")
    @PostMapping("/register")
    public Response register(@RequestBody @Valid RegisterCmd cmd) {
        registerService.register(cmd);
        return Response.buildSuccess();
    }

    @ApiOperation("后台-添加账户")
    @PostMapping("/add")
    public Response add(@RequestBody @Valid AddAccountCmd cmd) {
        registerService.add(cmd);
        return Response.buildSuccess();
    }

    @GetMapping("/list")
    @ApiOperation("用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "accountId", value = "用户Id", dataTypeClass = Long.class),
            @ApiImplicitParam(name = "email", value = "邮箱", dataTypeClass = String.class),
            @ApiImplicitParam(name = "inviteCode", value = "用户的邀请码", dataTypeClass = String.class),
            @ApiImplicitParam(name = "pageIndex", value = "当前页数", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataTypeClass = Integer.class)
    })
    public PageResponse<AccountVO> getList(@RequestParam(value = "accountId", required = false) Long accountId,
                                           @RequestParam(value = "email", required = false) String email,
                                           @RequestParam(value = "inviteCode", required = false) String inviteCode,
                                           @RequestParam(value = "pageIndex", defaultValue = "1", required = false) Integer pageIndex,
                                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
        return accountService.getList(accountId, email, inviteCode, pageIndex, pageSize);
    }

    @PostMapping("getNextList")
    @ApiOperation("获得下级信息列表")
    public ArrayResponse<AccountRebateVo> getNextList(@RequestParam("accountId") Long accountId) {
        return ArrayResponse.of(accountRelationService.listNext(accountId, ""));
    }

    @PostMapping("updateStatus")
    @ApiOperation("更新账号状态")
    public Response updateStatus(@RequestBody @Valid UpdateStatusCmd updateStatusCmd) {
        accountService.updateStatus(updateStatusCmd);
        return Response.buildSuccess();
    }

    @GetMapping("getVipFee")
    @ApiOperation("获得VIP会员费")
    public SingleResponse<BigDecimal> getVipFee() {
        return SingleResponse.of(vipService.getCurrentLoginVipFee());
    }

    @GetMapping("getTotalRebate")
    @ApiOperation("获得总的返佣")
    public SingleResponse<BigDecimal> getTotalRebate() {
        return SingleResponse.of(rebateService.getTotalRebate());
    }

    @GetMapping("getLoginUsableBalance")
    @ApiOperation("获得当前登陆人的可用余额（不包含赠送金额）")
    public SingleResponse<BigDecimal> getLoginUsableBalance() {
        return SingleResponse.of(walletService.getLoginUsableBalance());
    }

    @GetMapping("getCanWithdrawRebate")
    @ApiOperation("获得可提现的返佣")
    public SingleResponse<BigDecimal> getCanWithdrawRebate() {
        return SingleResponse.of(walletService.getCanWithdrawRebate());
    }

}
