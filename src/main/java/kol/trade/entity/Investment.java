package kol.trade.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 客户跟单投资表
 * 投资不能正常进行有几类原因：1.客户的apikey异常（过期，余额不足，权限等） 2.客户的平台余额为负数无法正常进行
 * 只有当条件1和2同时满足才能进行正常的投资。
 *
 * @author guanzhenggang@gmail.com
 */
@Data
@Entity
@Table(name = "trade_investment")
@EqualsAndHashCode(callSuper = true)
public class Investment extends BaseEntity {
    /**
     * 关联交易所的apikey，
     * 策略投资ID为-1
     */
    @Column(nullable = false)
    private Long apiKeyId;

    /**
     * 账户id
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 策略id
     */
    @Column(nullable = false)
    private Long strategyId;

    /**
     * 本金，本金为客户跟单时设定的资金，设定后不可改变
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal principal = BigDecimal.ZERO;

    /**
     * 当前余额
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 已使用资金
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal useBalance = BigDecimal.ZERO;

    /**
     * true-实盘  false-模拟,策略的开仓属于模拟投资，用户的仓位属于实盘投资
     */
    @Column(nullable = false)
    private Boolean isReal = false;

    /**
     * 是否暂停 true-暂停中，无法执行开仓计划  false-正常进行
     */
    @Column(nullable = false)
    private Boolean isPause = false;

    /**
     * 暂停原因
     */
    @Column(length = 500)
    private String pauseReason;

    /**
     * 是否停止 true-投资已结束  false-投资正常进行
     */
    @Column(nullable = false)
    private Boolean isEnd = false;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 缓存交易所key
     */
    @JsonIgnore
    @Column(nullable = false, length = 1000)
    private String apiKey;

    /**
     * 策略熔断
     */
    @Column(precision = 20, scale = 3)
    private BigDecimal strategyFusing;

    /**
     * 邮箱
     */
    private String email;


}
