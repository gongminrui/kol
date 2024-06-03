package kol.config.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 账户佣金与返佣等级及规则
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "rebate_rule")
public class RebateRule extends BaseEntity {
    /**
     * 等级
     */
    @Column(nullable = false)
    private Long level;
    /**
     * 返佣比例
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rebate = BigDecimal.ZERO;
    /**
     * 规则条件
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rule = BigDecimal.ZERO;
    /**
     * 类型
     */
    @Enumerated(EnumType.STRING)
    private TypeEnum type;
    /**
     * 是否为默认规则
     */
    private Boolean isDefault;

    public enum TypeEnum {
        /**
         * 跟单手续费
         */
        FOLLOW_COST_TYPE,
        /**
         * 策略盈利返佣
         */
        STRATEGY_REBATE_TYPE,
        /**
         * 代理人邀请手续费返佣
         */
        INVITE_REBATE_TYPE
    }
}
