package kol.money.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 结算单
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "statement")
public class Statement extends BaseEntity {
    /**
     * 账户id
     */
    @Comment("账户id")
    @Column(nullable = false)
    private Long accountId;
    /**
     * 邮箱
     */
    @Comment("邮箱")
    @Column(nullable = false)
    private String email;
    /**
     * 结账单开始时间
     */
    @Comment("结账单开始时间")
    @Column(nullable = false)
    private Date startDate;
    /**
     * 结账单结束时间
     */
    @Comment("结账单结束时间")
    @Column(nullable = false)
    private Date endDate;
    /**
     * 结账金额
     */
    @Comment("结账金额")
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal amount;
    /**
     * 盈利
     */
    @Comment("盈利")
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal profit;
    /**
     * 跟单次数
     */
    @Comment("跟单次数")
    private Integer positionCount;
    /**
     * 盈利的跟单次数
     */
    @Comment("盈利跟单次数")
    private Integer winPositionCount;

    /**
     * 是否结清
     */
    @Comment("是否结清")
    private Boolean settle;
}
