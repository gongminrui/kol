package kol.account.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import kol.account.service.FindPasswordService;
import kol.account.service.ModifyPasswordService;
import kol.common.model.Response;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * @author guan
 */
@RestController
@Api(tags = "账户模块")
@RequestMapping("/api/account")
public class PasswordController {
    final ModifyPasswordService modifyPasswordService;
    final FindPasswordService findPasswordService;

    public PasswordController(ModifyPasswordService modifyPasswordService,
                              kol.account.service.FindPasswordService findPasswordService) {
        this.modifyPasswordService = modifyPasswordService;
        this.findPasswordService = findPasswordService;
    }

    @PostMapping("/password/modify")
    @ApiOperation("修改密码")
    public Response modify(@RequestBody @Valid ModifyPasswordRequest request) {
        Long accuntId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        modifyPasswordService.modifyPassword(accuntId, request.getOldPassword(), request.getNewPassword());
        return Response.buildSuccess();
    }

    @PostMapping("/password/find")
    @ApiOperation("找回密码")
    public Response find(@RequestBody @Valid FindPasswordRequest request) {
        findPasswordService.findPassword(request.getVcode(), request.getNewPassword());
        return Response.buildSuccess();
    }

    @Data
    public static class ModifyPasswordRequest {
        @ApiModelProperty("旧密码")
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        @ApiModelProperty("新密码")
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    @Data
    public static class FindPasswordRequest {
        @ApiModelProperty("验证码")
        @NotBlank(message = "验证码不能为空")
        private String vcode;
        @ApiModelProperty("新密码")
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
