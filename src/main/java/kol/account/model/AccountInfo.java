package kol.account.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 账户详情
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "account_info")
public class AccountInfo extends BaseEntity {
    /**
     * 用户ID
     */
    @Column(nullable = false, unique = true)
    private Long accountId;
    /**
     * 合伙人ID
     */
    private Long partnerId;
    /**
     * 助力人ID
     */
    private Long helperId;
    /**
     * 邀请人ID
     */
    private Long inviterId;
    /**
     * 邀请人邀请码
     */
    @Column(length = 8)
    private String inviteCode;
    /**
     * 跟单手续费比例
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal followCost = BigDecimal.ZERO;
    /**
     * 邀请返佣
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal inviteRebate = BigDecimal.ZERO;
}
