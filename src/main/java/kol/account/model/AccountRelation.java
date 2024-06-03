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
 * 账户关系表
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "account_relation")
public class AccountRelation extends BaseEntity {
    /**
     * 账户ID
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    /**
     * 上级id
     */
    private Long pId;
    /**
     * 关系树结构
     */
    @Column(name = "relation_tree")
    private String relationTree;
}
