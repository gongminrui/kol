package kol.vcode.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import kol.common.model.Response;
import kol.vcode.model.VcodeService;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guan
 */
@Api(tags = "验证码")
@RestController
@RequestMapping("/api/vcode")
public class VcodeController {
    final VcodeService vcodeService;

    public VcodeController(VcodeService vcodeService) {
        this.vcodeService = vcodeService;
    }

    @ApiOperation("发送验证码")
    @PostMapping("/send")
    public Response send(@RequestBody EmailVo request) {
        vcodeService.send(request.getEmail());
        return Response.buildSuccess();
    }

    @ApiModel
    @Data
    public static class EmailVo {
        @ApiModelProperty("邮箱")
        private String email;
    }
}
