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
@Entity(name = "notice")
@Data
@Accessors(chain = true)
public class Notice extends BaseEntity {
    /**
     * 公告内容
     */
    @Column(name = "notice_content", nullable = false)
    private String noticeContent;
    /**
     * 状态
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    /**
     * 英文公告内容
     */
    @Column(name = "en_content", nullable = false)
    private String enContent;
}
