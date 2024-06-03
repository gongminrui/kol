package kol.trade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kol.common.model.BaseEntity;
import kol.trade.enums.ExchangeEnum;
import kol.trade.enums.PositionSideEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author guan
 */
@Data
@Entity
@Table(name = "trade_strategy")
@EqualsAndHashCode(callSuper = true)
public class Strategy extends BaseEntity {
    /**
     * 交易员账户id
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 策略名称
     */
    @Column(nullable = false, length = 20)
    private String name;

    /**
     * ON-正常  OFF-停止
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEnum status = StatusEnum.ON;

    /**
     * 交易品种
     */
    @Column(nullable = false, length = 20)
    private String symbol;

    /**
     * 杠杆倍数
     */
    @Column(nullable = false)
    private Integer leverage;

    /**
     * 交易所
     */
    @Enumerated(EnumType.STRING)
    private ExchangeEnum exchange;

    /**
     * 策略标签
     */
    @Column(length = 100)
    private String tags;

    /**
     * 策略描述
     */
    @Column(length = 1000)
    private String description;

    /**
     * 本金
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal principal = BigDecimal.ZERO;

    /**
     * 余额
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 最大余额，用于计算回测
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal maxBalance = BigDecimal.ZERO;

    /**
     * 累计收益率 统计数据，每次平仓后统计
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal profitRate = BigDecimal.ZERO;

    /**
     * 盈利次数
     */
    @Column(nullable = false)
    private Integer winCount = 0;

    /**
     * 亏损次数
     */
    @Column(nullable = false)
    private Integer lossCount = 0;

    /**
     * 最大回撤率 统计数据，每次平仓后统计
     */
    @Column(nullable = false, precision = 20, scale = 5)
    private BigDecimal maximumDrawdown = BigDecimal.ZERO;

    /**
     * 密钥
     */
    @JsonIgnore
    @Column(nullable = false, length = 500)
    private String secretKey;

    /**
     * 每人最小跟单资金
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal minAmount = new BigDecimal("100");

    /**
     * 每人最大跟单资金
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal maxAmount = new BigDecimal("10000");

    /**
     * 策略跟单手续费返佣比例
     */
    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal rebate = BigDecimal.ZERO;

    /**
     * 最大跟单人数
     */
    @Column(nullable = false, length = 10)
    private Integer bigFollowCount = 100;
    /**
     * 已跟单人数
     */
    @Column(nullable = false, length = 10)
    private Integer alreadyFollowCount = 0;
    /**
     * 策略类型
     */
    @Column(nullable = false)
    private Integer type;
    /**
     * 需要的vip等级
     */
    @Column(columnDefinition = "integer default 0")
    private Integer vipLevel=0;
    /**
     * 获取利润曲线图，跟时间没有关系，是按笔的一个利润曲线
     *
     * @return
     */
    @Transient
    public List<BigDecimal> profitRateHistory;
    /**
     * 最后更新时间
     */
    @Transient
    private Date lastUpdateDate;
    /**
     * 最后开平仓方向
     */
    @Transient
    private PositionSideEnum lastPositionSide;
    /**
     * 最后开平仓价格
     */
    @Transient
    private BigDecimal lastPrice;
    


    public enum StatusEnum {
        ON,
        OFF
    }
}
