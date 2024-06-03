package kol.message.listener;

import kol.message.event.MessageEvent;
import kol.message.service.MessageService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:57
 */
@Component
public class MessageListener {

    @Resource
    private MessageService messageService;

    @EventListener(MessageEvent.class)
    public void handle(MessageEvent messageEvent) {
        messageService.saveMessage(messageEvent);
    }
}
