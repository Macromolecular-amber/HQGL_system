package com.logistics.dto.message;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 消息视图：包含 SysMessage 全部字段，并补充发送人姓名、类型中文名与业务链接
 */
@Data
public class MessageVO {

    /** id */
    private Long id;

    /** 接收人ID */
    private Long receiverId;

    /** 发送人ID，0 表示系统发送 */
    private Long senderId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 消息类型 SYSTEM/BUSINESS/WARNING/APPROVAL */
    private String messageType;

    /** 业务模块 */
    private String bizModule;

    /** 关联业务单号 */
    private String bizOrderNo;

    /** 是否已读 */
    private Boolean isRead;

    /** 阅读时间 */
    private OffsetDateTime readTime;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 发送人姓名（0 为系统） */
    private String senderName;

    /** 消息类型中文名 */
    private String messageTypeLabel;

    /** 关联业务单号的链接 */
    private String relativeUrl;
}
