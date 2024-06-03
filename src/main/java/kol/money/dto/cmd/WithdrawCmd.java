package kol.money.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-03-03 20:32
 */
@Data
@ApiModel(description = "提现审核参数")
public class WithdrawCmd {
    /**
     * 申请提现金额
     */
    @ApiModelProperty("申请提现金额")
    @Min(value = 1, message = "提现金额必须大于0")
    private BigDecimal amount;
    @ApiModelProperty("验证码")
    @NotBlank(message = "验证码不能为空")
    private String vcode;
    @ApiModelProperty("提现地址")
    @NotBlank(message = "提现地址不能为空")
    private String addr;
}
