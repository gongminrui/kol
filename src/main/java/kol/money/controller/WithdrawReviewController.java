package kol.money.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kol.common.model.PageResponse;
import kol.common.model.Response;
import kol.money.dto.cmd.HandleWithdrawCmd;
import kol.money.dto.cmd.SelectWithdrawCmd;
import kol.money.dto.cmd.WithdrawCmd;
import kol.money.model.WithdrawReview;
import kol.money.service.WithdrawReviewService;
import kol.vcode.model.VcodeService;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author Gongminrui
 * @date 2023-03-04 10:27
 */
@Api(tags = "提现申请审核接口")
@RestController
@RequestMapping("/api/withdraw")
public class WithdrawReviewController {
    @Resource
    private WithdrawReviewService withdrawReviewService;


    /**
     * 发送提现验证码
     *
     * @return
     */
    @PostMapping("sendWithdrawVerify")
    @ApiOperation("发送提现验证码")
    public Response sendWithdrawVerify() {
        withdrawReviewService.sendWithdrawVerify();
        return Response.buildSuccess();
    }

    /**
     * 申请提现
     *
     * @param withdrawCmd
     * @return
     */
    @PostMapping("applyWithdraw")
    @ApiOperation("用户申请提现")
    public Response applyWithdraw(@RequestBody @Valid WithdrawCmd withdrawCmd) {
        withdrawReviewService.applyWithdraw(withdrawCmd);
        return Response.buildSuccess();
    }


    @PostMapping("handleApply")
    @ApiOperation("处理提现申请")
    public Response handleApply(@RequestBody @Valid HandleWithdrawCmd handleWithdrawCmd) {
        withdrawReviewService.handleApply(handleWithdrawCmd);
        return Response.buildSuccess();
    }

    /**
     * 获得提现审核翻页数据
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @GetMapping("pageWithdrawReview")
    @ApiOperation("获得提现审核翻页数据")
    public PageResponse<WithdrawReview> pageWithdrawReview(@RequestParam("pageIndex") int pageIndex,
                                                           @RequestParam("pageSize") int pageSize) {
        Assert.isTrue(pageIndex > 0, "页码参数不正确");
        Assert.isTrue(pageIndex > 0, "页数参数不正确");
        SelectWithdrawCmd selectWithdrawCmd = new SelectWithdrawCmd();
        selectWithdrawCmd.setPageIndex(pageIndex);
        selectWithdrawCmd.setPageSize(pageSize);
        return withdrawReviewService.pageWithdrawReview(selectWithdrawCmd);
    }

}
