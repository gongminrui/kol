package kol.account.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.account.model.RoleEnum;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kent
 */
@Data
@ApiModel("添加账户")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddAccountCmd {
    @ApiModelProperty(value = "邮箱", required = true)
    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "email不能为空")
    private String email;

    @ApiModelProperty(value = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @ApiModelProperty(value = "角色", required = true)
    @NotNull(message = "角色不能为空")
    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @ApiModelProperty(value = "邀请码")
    private String inviteCode;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private String vcode;
}
