package kol.account.model;

/**
 * @author kent
 */

public enum AccountStatusEnum {
    /**
     * 正常
     */
    NORMAL,
    /**
     * 禁用
     */
    DISABLE,
    /**
     * 账号存在为支付账单，而被冻结
     */
    FREEZE
}
