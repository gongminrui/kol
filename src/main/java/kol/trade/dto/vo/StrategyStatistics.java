package kol.trade.dto.vo;

import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-04-17 19:34
 */
public interface StrategyStatistics {
    Long getStrategyId();
    BigDecimal getWin();
    Integer getWinCount();
    BigDecimal getLoss();
    Integer getLossCount();
}
