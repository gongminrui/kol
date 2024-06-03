package kol.money.model;


import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "withdraw_review")
public class WithdrawReview extends BaseEntity {
    /**
     * 账户id
     */
    @Column(nullable = false)
    private Long accountId;
    /**
     * email
     */
    @Column(nullable = false, length = 50)
    private String email;
    /**
     * 提现账号
     */
    @Column(nullable = false)
    private String walletAddr;
    /**
     * 提现金额
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal walletAmount = BigDecimal.ZERO;
    /**
     * 是否已审核
     */
    private Boolean review;
    /**
     * 是否审核通过
     */
    private Boolean pass;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 提现地址
     */
    private String withdrawAddr;
}
