package kol.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.contek.invoker.okx.api.rest.user.PostTradeOrder;
import kol.account.model.Account;
import kol.account.model.RoleEnum;
import kol.account.repo.AccountRepo;
import kol.common.model.MessageType;
import kol.common.utils.JsonUtils;
import kol.message.event.MessageEvent;
import kol.trade.enums.ExchangeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-03 20:36
 */
@Slf4j
public abstract class BaseService {

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;
    @Resource
    private AccountRepo accountRepo;

    protected Account getLoginAccount() {
        return accountRepo.findById(getCurrentAccountId()).get();
    }

    /**
     * 当前登录用户是否管理员
     *
     * @return
     */
    protected boolean isAdmin() {
        return RoleEnum.ROLE_ADMIN == getLoginAccount().getRole();
    }

    /**
     * 获得当前请求的账号id
     *
     * @return
     */
    protected Long getCurrentAccountId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 发送消息通知
     *
     * @param account
     * @param title
     * @param content
     */
    protected void sendMessage(Long account, MessageType title, MessageType content) {
        sendMessage(account, title, content, null);
    }

    protected void sendMessage(Long account, MessageType title, MessageType content, List<Object> params) {
        applicationEventPublisher.publishEvent(new MessageEvent(account, title, content, params));
    }

    protected void handleMessage(Long accountId, ExchangeEnum exchangeEnum, String errorMsg, MessageType title) {
        try {
            String code = null;
            switch (exchangeEnum) {
                case OKX:
                    if (JsonUtils.isJson(errorMsg)) {
                        JsonNode root = JsonUtils.getJsonNode(errorMsg);
                        JsonNode error = root.get("data").get(0);
                        code = error.get("sCode").asText();
                    }
                    break;
                default:
                    break;
            }
            if (StringUtils.isBlank(code)) {
                sendMessage(accountId, title, MessageType.of(errorMsg));
            } else {
                sendMessage(accountId, title, MessageType.ofExchange(exchangeEnum, code));
            }
        } catch (Exception ex) {
            log.info("发送消息出错：{}", ex);
        }
    }
}
