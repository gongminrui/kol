package kol.message.event;

import kol.common.model.MessageType;
import kol.message.model.Message;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:54
 */
@Getter
public class MessageEvent extends ApplicationEvent {
    private Long accountId;
    private MessageType title;
    private MessageType content;
    private List<Object> params;

    public MessageEvent(Long accountId, MessageType title, MessageType content, List<Object> params) {
        super("");
        this.accountId = accountId;
        this.title = title;
        this.content = content;
        this.params = params;
    }
}
