package kol.money.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 链上转账记录,包含充值和提币
 *
 * @author guanzhenggang@gmail.com
 */
@Entity
@Data
@Accessors(chain = true)
@Table(name = "money_chain_record", uniqueConstraints = {@UniqueConstraint(columnNames = {"accountId", "chain", "txid"})})
@EqualsAndHashCode(callSuper = true)
public class MoneyChainRecord extends BaseEntity {
    /**
     * 账户ID
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 付款人地址
     */
    @Column(nullable = false, length = 256)
    private String fromAddr;

    /**
     * 收款人地址
     */
    @Column(nullable = false, length = 256)
    private String toAddr;

    /**
     * 链上交易ID
     */
    @Column(nullable = false, length = 256, unique = true)
    private String txid;

    /**
     * 金额
     */
    @Column(nullable = false, scale = 10, precision = 20)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * 链类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChainEnum chain;

    /**
     * 资金流水记录ID
     */
    @Column(nullable = false)
    private Long moneyRecordId;
}
