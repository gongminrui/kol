package kol.trade.dto.vo;

import java.math.BigDecimal;

/**
 * @author Gongminrui
 * @date 2023-04-17 22:14
 */
public interface PositionSumProfit {
    Long getAccountId();
    BigDecimal getProfit();
    Integer getTotalCount();
    Integer getWinCount();
}
