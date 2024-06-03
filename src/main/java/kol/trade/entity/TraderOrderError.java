package kol.trade.entity;

import kol.common.model.BaseEntity;
import kol.trade.enums.TradeTypeEnum;
import lombok.Data;

import javax.persistence.*;

/**
 * @author kent
 */
@Data
@Entity
@Table(name = "trade_order_error")
public class TraderOrderError extends BaseEntity {
    @Column(nullable = false)
    private Long accountId;
    @Column(nullable = false)
    private Long strategyId;
    @Column(nullable = false)
    private Long apiKeyId;
    /**
     * 开仓编号
     */
    @Column(nullable = false)
    private Long positionNo = 0L;
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TradeTypeEnum tradeType;
    @Column(length = 1000)
    private String errorMessage;
    @Column(length = 1000)
    private String investStr;
    @Column(length = 1000)
    private String requestCmd;
}
