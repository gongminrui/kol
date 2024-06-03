package kol.trade.dto.cmd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Gongminrui
 * @date 2023-03-19 16:26
 */
@Data
public class PageInvestmentCmd {
    @ApiModelProperty("策略id")
    @NotNull(message = "策略id不能为空")
    private Long strategyId;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("页码")
    private int pageIndex;
    @ApiModelProperty("页数")
    private int pageSize;
}
