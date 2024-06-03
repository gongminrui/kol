package kol.common.model;

import kol.trade.enums.ExchangeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 消息类型
 *
 * @author Gongminrui
 * @date 2023-03-19 10:36
 */
@Getter
@AllArgsConstructor
public enum MessageType {

    ERROR("通用错误", "", "系统错误，请联系客服", "System error, please contact customer service"),
    OPEN_FAILD("开仓失败", "", "开仓失败", "Opening failure"),
    CLOSE_FAILD("平仓失败", "", "平仓失败", "Closing failure"),
    WITHDRAW_FAILD("提现失败", "", "提现失败", "Withdrawal failure"),
    BILLING_INFO("账单通知标题", "", "账单信息", "Billing info"),
    BILLING_INFORM("账单通知内容", "", "你的账单已生成，未避免账户被冻结，请保证余额充足。",
            "Your bill has been generated, the account has not been frozen, please ensure that the balance is sufficient"),
    BILLING_BALANCE_FAIL_TITLE("账单结算失败", "", "账单结算失败", "Bill settlement failure"),
    BILLING_BALANCE_FAIL("账单结算失败", "", "账单结算失败，请您检查余额。",
            "Bill settlement failed, please check the balance."),
    ACCOUNT_DISABLE("账号被禁用", "", "账号被禁用", "The account is disabled."),
    VIP_EXPIRE_TITLE("会员到期提醒", "", "会员到期提醒", "Membership expiration reminder"),
    VIP_EXPIRE_CONTENT("会员到期提醒", "", "您的会员资格已到期,到期后所有跟单策略将自动停止，如需继续跟单请充值后手动开通会员。",
            "Your membership has expired, after the expiration of all documentary policies will be automatically stopped, if you need to continue the documentary, please recharge and manually open the membership."),
    VIP_RENEW_TITLE("会员续费提醒", "", "会员续费提醒", "Membership renewal reminder"),
    VIP_RENEW_CONTENT("会员续费提醒", "", "您的会员资格即将在{1}到期，系统会在{1}自动扣费，续期费用{2}U，请保证余额不低于{2}U，若扣费不足，可能导致会员禁用，从而影响正常跟单",
            "Your membership is about to expire at {1}, the system will automatically deduct the fee at {1}, the renewal fee {2}U, please ensure that the balance is not less than {2}U, if the deduction fee is insufficient, it may cause the member to disable, thus affecting the normal documentary"),
    NOT_SUFFICIENT_FUNDS("小于交易所最小开仓量，无法开仓。", "", "余额不足", "not sufficient funds"),
    EARNEST_MONEY_DEFICIENCY("余额不足，停止策略跟单", "", "余额不足，停止策略跟单", "balance is insufficient, stop the strategy copy"),
    OKX_ERROR_50100("欧易错误码50100", ExchangeEnum.OKX.name() + "_50100", "Api 已被冻结，请联系客服处理",
            "Api has been frozen, please contact customer service"),
    OKX_ERROR_50104("欧易错误码50104", ExchangeEnum.OKX.name() + "_50104", "无效的授权",
            "Invalid authorization"),
    OKX_ERROR_50101("欧易错误码50101", ExchangeEnum.OKX.name() + "_50101", "APIKey 与当前环境不匹配",
            "The APIKey does not match the current environment");

    private String msg;
    private String code;

    private String zh;
    private String en;


    public static MessageType of(String value) {
        if (StringUtils.isBlank(value)) {
            return ERROR;
        }
        return Arrays.stream(values()).filter(v -> v.getMsg().equals(value)).findFirst().orElse(ERROR);
    }

    public static MessageType ofExchange(ExchangeEnum exchangeEnum, String code) {
        if (StringUtils.isBlank(code)) {
            return ERROR;
        }
        String tmpCode = exchangeEnum.name() + "_" + code;
        return Arrays.stream(values()).filter(v -> v.getCode().equals(tmpCode)).findFirst().orElse(ERROR);
    }

}
