package com.logistics.dto.dashboard;

import lombok.Data;

/**
 * 消息通知项
 */
@Data
public class MessageVO {

    /** 消息ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 时间 */
    private String time;

    /** 类型 */
    private String type;
}
