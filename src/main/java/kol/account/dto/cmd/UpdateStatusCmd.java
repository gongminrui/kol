package kol.account.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.account.model.AccountStatusEnum;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kent
 */
@Data
@ApiModel(description = "更新状态参数")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStatusCmd {
    @ApiModelProperty(value = "更新状态用户的id", required = true)
    @NotNull(message = "accountId不能为空")
    private Long accountId;

    @ApiModelProperty(value = "状态", required = true)
    @NotNull(message = "状态参数错误")
    private AccountStatusEnum status;
}
