package kol.trade.dto.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class PositionVo {
	@ApiModelProperty(value = "positionId",required = true)
	Long positionId;
	@ApiModelProperty(value = "stopLossPrice",required = false)
	BigDecimal stopLossPrice;
	@ApiModelProperty(value = "stopGainPrice",required = false)
	BigDecimal stopGainPrice;
	@ApiModelProperty(value = "positionId",required = false)
	Long strategyId;
}
