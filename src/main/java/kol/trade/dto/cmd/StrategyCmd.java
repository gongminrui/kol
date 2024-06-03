package kol.trade.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.trade.entity.Strategy;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@ApiModel("添加|编辑策略")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyCmd {
    @ApiModelProperty(value = "编辑时传入")
    private Long id;

    @ApiModelProperty(value = "策略名称", required = true)
    @NotBlank(message = "策略名称不能为空")
    private String name;

    @ApiModelProperty(value = "交易对", required = true)
    @NotBlank(message = "交易对不能为空")
    private String symbol;

    @ApiModelProperty(value = "杠杆", required = true)
    @NotNull(message = "杠杆不能为空")
    private Integer leverage;

    @ApiModelProperty(value = "交易所", required = true)
    @NotNull(message = "exchange不能为空")
    private ExchangeEnum exchange;

    @ApiModelProperty(value = "策略标签", required = true)
    @NotBlank(message = "策略标签对不能为空")
    private String tags;

    @ApiModelProperty(value = "每人最小跟单资金", required = true)
    @NotNull(message = "minAmount不能为空")
    @DecimalMin(value = "100", message = "最小跟单资金不能小于100U")
    private BigDecimal minAmount;

    @ApiModelProperty(value = "每人最大跟单资金", required = true)
    @NotNull(message = "maxAmount不能为空")
    @DecimalMin(value = "10000", message = "最大跟单资金不能小于10000U")
    private BigDecimal maxAmount;

    @ApiModelProperty(value = "最大跟单人数", required = true)
    @NotNull(message = "bigFollowCount最大跟单人数不能为空")
    @Min(value = 10, message = "跟单人数不能小于10人")
    private Integer bigFollowCount;

    @ApiModelProperty("策略描述")
    private String description;

    @ApiModelProperty(value = "编辑时传入")
    private Strategy.StatusEnum status;

    private Integer type;
    
    @ApiModelProperty(value = "vip等级 0=普通 1=会员")
    private Integer vipLevel=0;
}
