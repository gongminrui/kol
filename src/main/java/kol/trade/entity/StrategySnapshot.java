package kol.trade.entity;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 策略盈利快照
 *
 * @author kent
 */
@Data
@Entity
@Table(name = "strategy_snapshot")
@EqualsAndHashCode(callSuper = true)
public class StrategySnapshot extends BaseEntity {
    @Column(nullable = false)
    private Long strategyId;

    /**
     * 本金
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal principal = BigDecimal.ZERO;

    /**
     * 余额
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 累计收益率 统计数据，每次平仓后统计
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal profitRate = BigDecimal.ZERO;

    /**
     * 最大回撤率 统计数据，每次平仓后统计
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal maximumDrawdown = BigDecimal.ZERO;

    /**
     * 盈利次数
     */
    @Column(nullable = false)
    private Integer winCount = 0;

    /**
     * 亏损次数
     */
    @Column(nullable = false)
    private Integer lossCount = 0;


}
