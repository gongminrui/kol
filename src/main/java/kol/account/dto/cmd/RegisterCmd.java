package kol.account.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * @author kent
 */
@Data
@ApiModel("注册请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterCmd {
    @ApiModelProperty(value = "邮箱", required = true)
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "email不能为空")
    private String email;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "验证码", required = true)
    @NotBlank(message = "验证码不能为空")
    private String vcode;

    @ApiModelProperty(value = "邀请码")
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
