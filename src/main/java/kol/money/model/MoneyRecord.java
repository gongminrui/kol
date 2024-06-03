package kol.money.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 账户资金变动记录,各种转账类型都以此表来扩展信息
 *
 * @author guanzhenggang@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "money_record")
@Accessors(chain = true)
@Entity
public class MoneyRecord extends BaseEntity {
    /**
     * 账户ID
     */
    @Column(nullable = false)
    private Long accountId;

    /**
     * 资金记录 负数代表扣  正数代表加 查余额只需要 sum(amount)
     */
    @Column(nullable = false, precision = 20, scale = 10)
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * 类型
     * CHAIN-链上交易
     * NEW_USER_DONATE-新用户赠送
     * TRADE_COST-交易服务费，针对客户
     * TRADE_REWARD-交易报酬，针对交易员
     * INTRODUCE_REWORD-推荐奖励，针对客户
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Type type;

    /**
     * 备注
     */
    @Column(length = 300)
    private String comment;

    /**
     * 钱包地址
     */
    @Column(length = 300)
    private String walletAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.UNREVIEW;

    public enum Status {
        /**
         * 未审核,添加记录都是未审核，添加定时任务审核
         */
        UNREVIEW,
        /**
         * 已审核,提现手动转账号修改为已审核
         */
        REVIEWED
    }

    public enum Type {
        /**
         * 链上充值
         */
        CHAIN_RECHARGE,
        /**
         * 链上提现
         */
        CHAIN_CASH_OUT,
        /**
         * 新用户赠送
         */
        NEW_USER_DONATE,
        /**
         * 交易盈利扣服务费，针对客户
         */
        TRADE_COST,
        /**
         * 交易亏损返服务费，针对客户
         */
        TRADE_BACK_COST,
        /**
         * 交易报酬，针对交易员
         */
        TRADE_REWARD,
        /**
         * 推荐奖励，针对合伙人
         */
        INTRODUCE_REWORD,
        /**
         * vip续费
         */
        VIP_FEE,
        /**
         * 账单结算
         */
        STATEMENT_SETTLE
    }
}
