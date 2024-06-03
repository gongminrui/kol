package kol.auth.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import kol.auth.model.AuthService;
import kol.common.model.Response;
import kol.common.model.SingleResponse;
import kol.common.utils.AESUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

/**
 * @author guan
 */
@RestController
@Api(tags = "认证模块")
@RequestMapping("/api/auth")
public class AuthController {

    AESUtils aesUtils;
    final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    @ApiOperation("登录")
    public SingleResponse<LoginRequest> login(@RequestBody LoginForm loginForm, HttpServletRequest request) {
        String token = authService.login(loginForm.getEmail(), loginForm.getPassword(), request.getRemoteAddr(), loginForm.getClientType());
        LoginRequest tokenVo = new LoginRequest().setToken(token);
        return SingleResponse.of(tokenVo);
    }

    @PostMapping("/logout")
    @ApiOperation("退出")
    public Response logout(@RequestHeader(com.google.common.net.HttpHeaders.AUTHORIZATION) String token) {
        authService.logout(token);
        return Response.buildSuccess();
    }

    @Data
    @ApiModel
    @Accessors(chain = true)
    public static class LoginForm {
        @ApiModelProperty("邮箱")
        @NotBlank(message = "邮箱不能为空")
        private String email;

        @ApiModelProperty("密码")
        @NotBlank(message = "密码不能为空")
        private String password;

        @ApiModelProperty("客户端类型")
        private String clientType;
    }

    @Data
    @Accessors(chain = true)
    @ApiModel
    public static class LoginRequest {
        @ApiModelProperty("访问令牌,后续请求需要在header中设置Authorization: token")
        private String token;
    }

    @Data
    @Accessors(chain = true)
    @ApiModel
    public static class EncryptTokenReseponse {
        @ApiModelProperty("访问令牌")
        private String token;

        @ApiModelProperty("时间戳")
        private Long timstamp;

        @ApiModelProperty("加密后的TOKEN")
        private String encryptedToken;
    }

}
