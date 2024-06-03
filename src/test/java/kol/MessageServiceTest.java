package kol;

import kol.auth.model.TokenAuthentication;
import kol.common.model.BaseDTO;
import kol.common.model.PageResponse;
import kol.message.event.MessageEvent;
import kol.message.model.Message;
import kol.message.repo.MessageRepo;
import kol.message.service.MessageService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Gongminrui
 * @@date 2023-03-03 17:13
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageServiceTest {
    @Resource
    private MessageService messageService;
    @Resource
    private MessageRepo messageRepo;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    private static final long accountId = 123123l;

    private static Long id;

    @Test
    @Order(1)
    public void init() {
//        messageRepo.deleteAll();
//        SecurityContextHolder.setStrategyName("1231232");
    }

    @Test
    @Order(2)
    public void getUnreadCount() {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int unreadCount = messageService.getUnreadCount();
        Assert.isTrue(unreadCount > 0, "");
    }

    @Test
    @Order(3)
    public void listByAccountId() {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        List<Message> messages = messageService.listByAccountId();
        Assert.isTrue(messages.size() > 0, "");
    }

    @Test
    @Disabled
    @Order(4)
    public void setRead() {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        BaseDTO baseDTO = new BaseDTO();
        baseDTO.setId(id);
        messageService.setRead(baseDTO);

        Optional<Message> optional = messageRepo.findById(id);
        Assert.isTrue(!optional.get().getUnread(), "");
    }

    @Test
    @Order(5)
    public void clearUnRead() {
        Authentication authentication = new TokenAuthentication(accountId, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        messageService.clearUnread();
    }

    @Test
    public void pageByAccountId(){
        Authentication authentication = new TokenAuthentication(5l, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        PageResponse<Message> messagePageResponse = messageService.pageByAccountId(1, 30);
        System.out.println();
    }

}
