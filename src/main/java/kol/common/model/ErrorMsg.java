package kol.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误消息
 * @author Gongminrui
 * @date 2023-03-19 10:36
 */
@Getter
@AllArgsConstructor
public enum ErrorMsg {

    EMAIL_EXIST("10001", "邮箱已经注册"),
    INVITE_CODE_ERROR("10002", "邀请码错误"),
    NOT_BALANCE("10003", "余额不足"),
    RETRY_SUBMIT_WITHDRAW_REVIEW("10004", "重复提交提现审核"),
    VERIFY_CODE_ERROR("10005", "验证码错误"),
    IN_INVESTMENT("10006", "跟单中，不能提现"),
    NOT_POWER("10007", "无权限");

    private String code;
    private String msg;


}
