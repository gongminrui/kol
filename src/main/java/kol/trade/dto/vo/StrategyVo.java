package kol.trade.dto.vo;

import kol.trade.entity.Strategy;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-04-17 19:39
 */
@Data
public class StrategyVo extends Strategy {
    /**
     * 赢亏比
     */
    private BigDecimal winLossRate;
}
