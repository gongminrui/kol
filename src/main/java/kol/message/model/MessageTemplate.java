package kol.message.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:59
 */
@Entity(name = "message_template")
@Data
@Accessors(chain = true)
public class MessageTemplate extends BaseEntity {
    /**
     * 消息类型
     */
    @Column(name = "msg_type", nullable = false)
    private Integer msgType;
    /**
     * 标题
     */
    @Column(name = "zh_title", nullable = false)
    private String zhTitle;
    /**
     * 英文标题
     */
    @Column(name = "en_title", nullable = false)
    private String enTitle;
    /**
     * 消息内容
     */
    @Column(name = "zh_content", nullable = false)
    private String zhContent;
    /**
     * 英文内容
     */
    @Column(name = "en_content", nullable = false)
    private String enContent;
}
