package kol.money.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.common.model.BaseDTO;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-03-03 20:32
 */
@Data
@ApiModel(description = "处理提现申请参数")
public class HandleWithdrawCmd extends BaseDTO {
    /**
     * 是否通过
     */
    @ApiModelProperty("是否通过")
    @NotNull(message = "pass参数不能为空")
    private Boolean pass;
    /**
     * 拒绝时需要填写的备注
     */
    @ApiModelProperty("拒绝时需要填写的备注")
    private String remarks;
}
