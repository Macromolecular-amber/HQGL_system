package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_message 实体：站内消息中心
 */
@Data
@Entity
@Table(name = "sys_message")
public class SysMessage {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** receiver_id 接收人ID */
    @Column(name = "receiver_id")
    private Long receiverId;

    /** sender_id 发送人ID，0 表示系统发送 */
    @Column(name = "sender_id")
    private Long senderId;

    /** title 标题 */
    @Column(name = "title", length = 200)
    private String title;

    /** content 内容 */
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /** message_type 消息类型 SYSTEM/BUSINESS/WARNING/APPROVAL */
    @Column(name = "message_type", length = 20)
    private String messageType;

    /** biz_module 业务模块 */
    @Column(name = "biz_module", length = 50)
    private String bizModule;

    /** biz_order_no 关联业务单号 */
    @Column(name = "biz_order_no", length = 100)
    private String bizOrderNo;

    /** is_read 是否已读 */
    @Column(name = "is_read")
    private Boolean isRead;

    /** read_time 阅读时间 */
    @Column(name = "read_time")
    private OffsetDateTime readTime;

    /** create_by 创建人 */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time 创建时间 */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
