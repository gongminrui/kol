package kol.trade.dto.vo;

import io.swagger.annotations.ApiModel;
import kol.trade.entity.Position;
import lombok.Data;

/**
 * 策略仓位
 *
 * @author kent
 */
@Data
@ApiModel("跟投策略仓位信息")
public class InvestmentPositionVO {
    private Long investmentId;
    private Long strategyId;
    private String strategyName;
    private String isPause;
    private Position position;
}
