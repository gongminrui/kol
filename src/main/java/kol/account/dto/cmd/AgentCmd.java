package kol.account.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@ApiModel("添加代理")
public class AgentCmd {
    @ApiModelProperty("成为代理账号ID")
    @NotNull(message = "代理账号ID不能为空")
    private Long accountId;

    @ApiModelProperty("返佣比例不大于自身返佣比例")
    @NotNull(message = "返佣比例不能为空")
    @DecimalMin(value = "0.001", message = "最小值为0.001")
    @DecimalMax(value = "1", message = "最大值为1")
    private BigDecimal inviteRebate;

}
