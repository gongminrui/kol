package kol.money.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @author jacky
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "rebate")
public class Rebate extends BaseEntity {
    @Comment("账户id")
    @Column(nullable = false, unique = true)
    private Long accountId;
    @Comment("总的返佣")
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal totalRebate = BigDecimal.ZERO;
    @Comment("已结算的返佣")
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal balanceRebate = BigDecimal.ZERO;
}
