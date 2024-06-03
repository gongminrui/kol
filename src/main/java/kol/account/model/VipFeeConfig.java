package kol.account.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 会员费配置
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "vip_fee_config")
public class VipFeeConfig extends BaseEntity {
    /**
     * 下级个数
     */
    @Column(name = "next_count", nullable = false)
    @Comment("下级个数")
    private Integer nextCount;
    /**
     * 折扣
     */
    @Comment("折扣")
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal discount;
}
