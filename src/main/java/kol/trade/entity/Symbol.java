package kol.trade.entity;

import kol.common.model.BaseEntity;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 交易对
 *
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "symbol")
public class Symbol extends BaseEntity {
    @Column(nullable = false, length = 30)
    private String symbolTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExchangeEnum exchange;

    public enum StatusEnum {
        ON,
        OFF
    }
}
