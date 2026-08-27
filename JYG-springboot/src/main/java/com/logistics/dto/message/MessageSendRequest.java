package com.logistics.dto.message;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 发送消息请求
 */
@Data
public class MessageSendRequest {

    /** 接收人ID列表 */
    @NotEmpty(message = "接收人不能为空")
    private List<Long> receiverIds;

    /** 标题 */
    @NotEmpty(message = "标题不能为空")
    private String title;

    /** 内容 */
    private String content;

    /** 消息类型：SYSTEM/BUSINESS/WARNING/APPROVAL */
    @NotEmpty(message = "消息类型不能为空")
    private String messageType;

    /** 业务模块 */
    private String bizModule;

    /** 关联业务单号 */
    private String bizOrderNo;
}
