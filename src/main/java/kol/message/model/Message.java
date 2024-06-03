package kol.message.model;

import kol.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Comment;

/**
 * @author Gongminrui
 * @date 2023-03-03 9:59
 */
@Entity(name = "message")
@Data
@Accessors(chain = true)
public class Message extends BaseEntity {
    /**
     * 账号id
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    /**
     * 标题
     */
    @Column(name = "title", nullable = false)
    private String title;
    /**
     * 消息内容
     */
    @Column(name = "msg_content", columnDefinition = "text", nullable = false)
    private String msgContent;
    /**
     * 消息类型
     */
    @Column(name = "msg_type", nullable = false)
    private Integer msgType;
    /**
     * 是否未读
     */
    @Column(name = "unread", nullable = false)
    private Boolean unread;
    /**
     * 英文标题
     */
    @Column(name = "en_title", nullable = false)
    private String enTitle;
    /**
     * 英文内容
     */
    @Column(name = "en_content", columnDefinition = "text", nullable = false)
    private String enContent;
 
    /**
     * 是否为弹出消息
     */
    @Column(columnDefinition = "TINYINT(1) default 0")
    @Comment("是否为弹出消息")
    private Boolean isPopMsg;
}
