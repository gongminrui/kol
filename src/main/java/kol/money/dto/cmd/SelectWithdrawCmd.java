package kol.money.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Gongminrui
 * @date 2023-03-03 21:22
 */
@Data
@ApiModel(description = "查询提现审核参数")
public class SelectWithdrawCmd {
    /**
     * 页码
     */
    @ApiModelProperty("页码")
    @Min(value = 1, message = "页码不能小于1")
    @Max(value = 10000, message = "页码不能小于10000")
    private int pageIndex;
    /**
     * 页数
     */
    @ApiModelProperty("页数")
    @Min(value = 1, message = "页数不能小于1")
    @Max(value = 1000, message = "页数不能小于1000")
    private int pageSize;
    /**
     * 是否已审核
     */
    @ApiModelProperty("是否已审核")
    private Boolean review;
    /**
     * 是否通过
     */
    @ApiModelProperty("是否通过")
    private Boolean pass;
    @ApiModelProperty("email")
    private String email;
}
