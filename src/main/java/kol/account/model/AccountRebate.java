package kol.account.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * 账户返佣关系
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "account_rebate")
public class AccountRebate extends BaseEntity {
    /**
     * 账户ID
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    /**
     * 返佣比例
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rebate;
    /**
     * 是否有效
     */
    @Column(nullable = false)
    private Boolean valid;
}
