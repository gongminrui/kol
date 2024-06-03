package kol.trade.entity;

import kol.common.model.BaseEntity;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.MarketEnum;
import kol.trade.enums.TradeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author kent
 */
@Data
@Entity
@EqualsAndHashCode(callSuper=false)
@Table(name = "trade_order")
public class TradeOrder extends BaseEntity {
    /**
     * 这个仓位所属账户投资ID，如果是模拟仓位则为空
     */
    private Long investmentId;

    /**
     * 这个仓位所属账户ID
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 这个仓位所属策略ID
     */
    @Column(nullable = false)
    private Long strategyId;

    /**
     * 仓位编号
     */
    @Column(nullable = false)
    private Long positionNo = 0L;

    /**
     * 交易对
     */
    @Column(nullable = false, length = 20)
    private String symbol;

    /**
     * 交易市场
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketEnum market;

    /**
     * 交易所
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeEnum exchange;

    /**
     * 杠杆倍数
     */
    @Column(nullable = false, precision = 20, scale = 0)
    private BigDecimal leverage;

    /**
     * 成交价格
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal avgPrice;

    /**
     * 交易量
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal vol;

    /**
     * 手续费
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal fee = BigDecimal.ZERO;

    /**
     * 交易所订单ID
     */
    @Column(length = 1000, nullable = false)
    private String exchangeOrderId;

    /**
     * 交易类型【开多、开空、平多、平空】
     * 说明：现货【开多、平多】
     */
    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeTypeEnum tradeType;

    /**
     * true-实盘  false-模拟,策略的开仓属于模拟仓位，用户的仓位属于实盘仓位
     */
    @Column(nullable = false)
    private Boolean isReal = false;
}
