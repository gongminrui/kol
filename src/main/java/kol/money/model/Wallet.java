package kol.money.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "wallet")
public class Wallet extends BaseEntity {
    @Column(nullable = false, unique = true)
    private Long accountId;
    /**
     * 当前余额（充值金额 + 返佣金额 + 赠送金额 - 提现金额 - 跟单服务费）
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal balance = BigDecimal.ZERO;
    /**
     * 赠送金额(正数)
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal giveAmount = BigDecimal.ZERO;
    /**
     * 返佣金额(正数)
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal rebateAmount = BigDecimal.ZERO;
    /**
     * 充值金额(正数)
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rechargeAmount = BigDecimal.ZERO;
    /**
     * 提现金额(正数)
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal cashOutAmount = BigDecimal.ZERO;
    /**
     * 跟单服务费(正数)
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal followCostAmount = BigDecimal.ZERO;
}
