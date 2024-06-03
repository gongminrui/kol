package kol.trade.entity;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 投资快照
 *
 * @author kent
 */
@Data
@Entity
@Table(name = "trade_invest_snapshot")
@EqualsAndHashCode(callSuper = true)
public class InvestSnapshot extends BaseEntity {

    /**
     * 投资ID
     */
    @Column(nullable = false)
    private Long investId;

    /**
     * 账户id
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 策略id
     */
    @Column(nullable = false)
    private Long strategyId;

    /**
     * 本金，本金为客户跟单时设定的资金，设定后不可改变
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal principal = BigDecimal.ZERO;

    /**
     * 当前余额
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 累计盈利率
     */
    @Column(nullable = false, precision = 10, scale = 5)
    private BigDecimal profitRate = BigDecimal.ZERO;
}
