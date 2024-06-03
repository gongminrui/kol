package kol.trade.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Comment;

import kol.common.model.BaseEntity;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.MarketEnum;
import kol.trade.enums.PositionSideEnum;
import kol.trade.enums.PositionStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓位
 *
 * @author guanzhenggang@gmail.com
 */
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "trade_position")
@Data
public class Position extends BaseEntity {
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
     * 交易对
     */
    @Column(nullable = false, length = 20)
    private String symbol;

    /**
     * 交易市场
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketEnum market = MarketEnum.PERP;

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
    private BigDecimal leverage = BigDecimal.ZERO;

    /**
     * 开仓量（存储数量，而非张数）
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal entryVol = BigDecimal.ZERO;

    /**
     * 进场价格
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal entryPrice = BigDecimal.ZERO;

    /**
     * 进场时间
     */
    @Column(nullable = false)
    private LocalDateTime entryTime;

    /**
     * 出场价格
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal exitPrice = BigDecimal.ZERO;

    /**
     * 平仓量（存储数量，而非张数）
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal exitVol = BigDecimal.ZERO;

    /**
     * 出场时间
     */
    private LocalDateTime exitTime;

    /***
     * 仓位方向
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositionSideEnum positionSide;

    /**
     * true-实盘  false-模拟,策略的开仓属于模拟仓位，用户的仓位属于实盘仓位
     */
    @Column(nullable = false)
    private Boolean isReal = false;

    /**
     * 仓位编号
     */
    @Column(nullable = false)
    private Long positionNo = 0L;

    /**
     * 跟单费用，根据系统配置的抽佣比例进行计算，只算盈利的仓位
     */
    @Column(precision = 20, scale = 5)
    private BigDecimal followCost = BigDecimal.ZERO;

    /**
     * 盈利
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal profit = BigDecimal.ZERO;

    /**
     * 盈利率
     */
    @Column(nullable = false, precision = 10, scale = 5)
    private BigDecimal profitRate = BigDecimal.ZERO;

    /**
     * 手续费
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal fee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PositionStatusEnum status = PositionStatusEnum.OPEN;

    @Column(nullable = true, precision = 20, scale = 10)
    @Comment("全仓止损价")
    private BigDecimal stopLossPrice;
    
    @Column(nullable = true, precision = 20, scale = 10)
    @Comment("全仓止盈价")
    private BigDecimal stopGainPrice;
    /**
     * 是否结算
     */
    private Boolean isSettlement = Boolean.FALSE;
    /**
     * 浮动盈亏金额(未扣除手续费亏损)
     */
    @Transient
    private BigDecimal floatingGrossProfit=BigDecimal.ZERO;
    /**
     * 根据最新价格更新浮动盈亏金额
     * @param currentPrice
     */
    public void updateFloatingGrossProfit(BigDecimal currentPrice) {
    	BigDecimal p=currentPrice.subtract(getEntryPrice()).divide(getEntryPrice(),4,RoundingMode.DOWN).multiply(getEntryVol().multiply(getEntryPrice()));
    	if(positionSide==PositionSideEnum.LONG) {
    		floatingGrossProfit=p;
    	}else {
    		floatingGrossProfit=p.negate();
    	}
    }
}
