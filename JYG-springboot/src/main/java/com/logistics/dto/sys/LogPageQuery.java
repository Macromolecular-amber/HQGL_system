package com.logistics.dto.sys;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 操作日志分页查询条件
 */
@Data
public class LogPageQuery {

    /** 用户名（模糊） */
    private String username;

    /** 模块 */
    private String module;

    /** 操作类型 */
    private String operationType;

    /** 开始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
