package com.logistics.dto.dashboard;

import lombok.Data;

/**
 * 待办审批项
 */
@Data
public class TodoVO {

    /** 待办ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 模块名 */
    private String module;

    /** 时间 */
    private String time;

    /** 状态 */
    private String status;
}
