package kol.message.service;

import kol.account.model.Account;
import kol.account.model.AccountStatusEnum;
import kol.account.repo.AccountRepo;
import kol.common.model.BaseDTO;
import kol.common.model.MessageType;
import kol.common.model.PageResponse;
import kol.common.service.BaseService;
import kol.common.utils.HttpUtil;
import kol.common.utils.StringUtil;
import kol.message.dto.cmd.MessageAddCmd;
import kol.message.event.MessageEvent;
import kol.message.model.Message;
import kol.message.repo.MessageRepo;
import kol.message.repo.MessageTemplateRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:59
 */
@Service
public class MessageService extends BaseService {
    @Resource
    private MessageRepo messageRepo;
    @Resource
    private MessageTemplateRepo messageTemplateRepo;
    @Resource
    AccountRepo accountRepo;

    /**
     * 获得消息
     *
     * @return
     */
    public List<Message> listByAccountId() {
        List<Message> list = messageRepo.findByAccountIdLimit(getCurrentAccountId(), 50);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    public PageResponse<Message> pageByAccountId(int pageIndex, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        PageRequest pageRequest = PageRequest.of(pageIndex - 1, pageSize, sort);

        Long currentAccountId = getCurrentAccountId();
        Specification<Message> specification = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(criteriaBuilder.equal(root.get("accountId"), currentAccountId));
        };

        Page<Message> page = messageRepo.findAll(specification, pageRequest);

        List<Message> content = page.getContent();
        content.forEach(v -> {
            if (HttpUtil.isEn()) {
                v.setMsgContent(v.getEnContent());
                v.setTitle(v.getEnTitle());
            }
        });

        return PageResponse.of(content, page.getTotalElements(), pageSize, pageIndex);
    }

    /**
     * 保存消息
     *
     * @param messageEvent
     * @return
     */
    public Long saveMessage(MessageEvent messageEvent) {
        Message message = new Message();
        message.setAccountId(messageEvent.getAccountId());

        MessageType title = messageEvent.getTitle();
        MessageType content = messageEvent.getContent();

        message.setTitle(title.getZh());
        message.setEnTitle(title.getEn());

        if (content != null) {
            String zh = content.getZh();
            String en = content.getEn();

            List<Object> params = messageEvent.getParams();
            if (!CollectionUtils.isEmpty(params)) {
                zh = StringUtil.replace(zh, params);
                en = StringUtil.replace(en, params);
            }

            message.setMsgContent(zh);
            message.setEnContent(en);
        }
        message.setMsgType(0);
        message.setUnread(true);

        messageRepo.save(message);
        return message.getId();
    }

    public void saveMessageAllAccount(MessageAddCmd cmd) {
        List<Account> accountList = accountRepo.findByStatusNot(AccountStatusEnum.DISABLE);
        List<Message> messageList = new ArrayList<>();
        accountList.stream().forEach(a -> {
            Message message = new Message();
            message.setAccountId(a.getId());
            message.setTitle(cmd.getTitle());
            message.setMsgContent(cmd.getMsgContent());
            message.setEnTitle(cmd.getEnTitle());
            message.setEnContent(cmd.getEnContent());
            message.setIsPopMsg(cmd.getIsPopMsg());
            message.setMsgType(0);
            message.setUnread(true);
            messageList.add(message);
        });
        messageRepo.saveAll(messageList);
    }

    /**
     * 清空未读
     *
     * @return
     */
    public int clearUnread() {
        List<Message> list = messageRepo.findByAccountIdAndUnread(getCurrentAccountId(), true);
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(v -> v.setUnread(false));
            messageRepo.saveAll(list);
        }
        return 1;
    }

    /**
     * 设置已读
     *
     * @param baseDTO
     * @return
     */
    public int setRead(BaseDTO baseDTO) {
        Optional<Message> optional = messageRepo.findById(baseDTO.getId());
        if (!optional.isPresent()) {
            return -1;
        }
        Long currentAccountId = getCurrentAccountId();
        Message message = optional.get();
        if (message.getAccountId().longValue() != currentAccountId.longValue()) {
            return -2;
        }
        message.setUnread(false);
        messageRepo.save(message);
        return 1;
    }

    /**
     * 获得未读消息数量
     *
     * @return
     */
    public int getUnreadCount() {
        return messageRepo.countByAccountIdAndUnread(getCurrentAccountId(), true);
    }

}
