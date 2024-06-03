package kol.trade.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 添加跟单
 *
 * @author kent
 */
@Data
@ApiModel("添加跟单")
public class InvestmentCmd {
    @ApiModelProperty("交易所KEY的id")
    @NotNull(message = "不能为空")
    private Long apiKeyId;

    @ApiModelProperty("策略Id")
    @NotNull(message = "策略Id不能为空")
    private Long strategyId;

    @ApiModelProperty("跟投资金")
    @NotNull(message = "跟投资金不能为空")
    @DecimalMin(value = "0", message = "最小跟投资金不能小于零")
    private BigDecimal principal;

    @JsonIgnore
    @ApiModelProperty(value = "是否真实投资", hidden = true)
    private Boolean isReal;

    @ApiModelProperty("策略熔断")
    @NotNull(message = "策略跟单熔断不能为空")
    private BigDecimal strategyFusing = BigDecimal.ONE;
}
