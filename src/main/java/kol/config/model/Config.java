package kol.config.model;

import lombok.Data;

import javax.persistence.*;

/**
 * @author guanzhenggang@gmail.com
 */
@Data
@Entity(name = "config")
public class Config {
    @Id
    @Enumerated(EnumType.STRING)
    private KeyEnum k;
    @Column(length = 5000)
    private String v;
    private String comment;

    public enum KeyEnum {
        /**
         * 新用户赠送金额
         */
        NEW_ACCOUNT_DONATE,
        /**
         * trc20收款地址,冷钱包
         */
        CHAIN_TRC20_ADDR,
        /**
         * trc20付款地址，热钱包
         */
        CHAIN_TRC20_PRIVATE,
        /**
         * erc20收款地址
         */
        CHAIN_ERC20_ADDR,
        /**
         * bsc收款地址
         */
        CHAIN_BSC_ADDR,
        /**
         * 服务条款
         */
        TERMS_OF_SERVICE,
        /**
         * 跟单抽佣比列
         */
        FOLLOW_COST_RATIO,
        /**
         * 邀请人返佣
         */
        INVITE_REBATE,
        /**
         * 合伙人最大返佣比例
         */
        PARTNER_MAX_REBATE,
        /**
         * 策略返利
         */
        STRATEGY_REBATE,
        /**
         * 可用交易所
         */
        EXCHANGE,
        /**
         * 是否启用VIP
         */
        ENABLE_VIP,
        /**
         * vip开通费用
         */
        VIP_FEE,
        /**
         * vip开通原价费用
         */
        VIP_FEE_ORIGIN,
        /**
         * 是否启用结算
         */
        ENABLE_SETTLE,
        /**
         * 服务器IP
         */
        SERVER_IP,
        /**
         * VIP关系一级返利
         */
        VIP_ONE_LEVEL_REBATE,
        /**
         * VIP关系二级返利
         */
        VIP_TWO_LEVEL_REBATE,
        /**
         * 账单是否净值模式
         */
        BILL_NET_VALUE,
        /**
         * 结算模式：每单：PER_ORDER、周期：CYCLE
         */
        STATE_MODE,
        /**
         * 注册赠送vip天数
         */
        REGISTER_GIVE_VIP_DAY,
        /**
         * 距离会员到期提醒的天数
         */
        VIP_EXPIRE_WARN_DAY,
        /**
         * 最小结算金额
         */
        MIN_SETTLE_AMOUNT,
        /**
         * 推荐策略
         */
        RECOMMEND_STRATEGY,
        /**
         * 平台名称
         */
        PLATFORM_NAME,
        /**
         * 邮件发件人
         */
        EMAIL_FROM
    }

}
