package kol.message.dto.cmd;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kent
 */
@Data
public class MessageAddCmd {
    /**
     * 标题
     */
    @NotBlank(message = "消息标题不能空")
    private String title;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能空")
    private String msgContent;

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private Integer msgType;

    /**
     * 英文标题
     */
    @NotBlank(message = "消息英文标题不能空")
    private String enTitle;

    /**
     * 英文内容
     */
    @NotBlank(message = "消息英文内容不能空")
    private String enContent;

    @NotNull(message = "是否为弹窗消息")
    private Boolean isPopMsg;
}
