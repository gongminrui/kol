package kol.trade.dto.cmd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import kol.trade.enums.CapitalModeEnum;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.MarketEnum;
import kol.trade.enums.TradeTypeEnum;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@ApiModel("市价开平仓")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCmd {

    @ApiModelProperty("策略ID")
    @NotNull(message = "策略ID不能为空")
    private Long strategyId;

    @ApiModelProperty("")
    @NotBlank(message = "交易对不能为空")

    private String symbol;
    /**
     * 交易类型（开多、开空、平多、平空）
     */
    @ApiModelProperty("交易类型（开多、开空、平多、平空）")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "交易类型不能为空")
    private TradeTypeEnum tradeType;

    /**
     * 开仓仓位
     */
    @ApiModelProperty("开|平仓位")
    @NotNull(message = "开平仓位不能为空")
    @DecimalMin(value = "0.01", message = "开平仓仓位最小为1%或者0.01")
    @DecimalMax(value = "1", message = "开平仓仓位最大值为100%或者1")
    private BigDecimal positionRatio;

    @ApiModelProperty("杠杆")
    @NotNull(message = "杠杆不能为空")
    @DecimalMin(value = "1.00", message = "杠杆最小倍数为1倍")
    @DecimalMax(value = "20.00", message = "杠杆最大倍数为20倍")
    private BigDecimal leverage;

    @ApiModelProperty("交易市场")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "交易市场不能为空")
    private MarketEnum market;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Long positionNo;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private ExchangeEnum exchange;
    
    @ApiModelProperty("本金模式,默认：REMAIND ,可选 REMAIND(余额) INIT（初始本金）")
    @Enumerated(EnumType.STRING)
    private CapitalModeEnum capitalMode=CapitalModeEnum.REMAIND;
    @ApiModelProperty("全仓止损价")
    private BigDecimal stopLossPrice;
    @ApiModelProperty("全仓止盈价")
    private BigDecimal stopGainPrice;
}
