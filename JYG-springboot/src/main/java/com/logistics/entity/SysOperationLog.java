package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_operation_log 实体
 */
@Data
@Entity
@Table(name = "sys_operation_log")
public class SysOperationLog {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** user_id */
    @Column(name = "user_id")
    private Long userId;

    /** username */
    @Column(name = "username", length = 50)
    private String username;

    /** real_name */
    @Column(name = "real_name", length = 50)
    private String realName;

    /** module */
    @Column(name = "module", length = 50)
    private String module;

    /** operation_type */
    @Column(name = "operation_type", length = 30)
    private String operationType;

    /** operation_desc */
    @Column(name = "operation_desc", length = 500)
    private String operationDesc;

    /** request_url */
    @Column(name = "request_url", length = 255)
    private String requestUrl;

    /** request_method */
    @Column(name = "request_method", length = 10)
    private String requestMethod;

    /** request_params */
    @Column(name = "request_params", columnDefinition = "text")
    private String requestParams;

    /** response_code */
    @Column(name = "response_code")
    private Integer responseCode;

    /** response_msg */
    @Column(name = "response_msg", length = 500)
    private String responseMsg;

    /** cost_time */
    @Column(name = "cost_time")
    private Integer costTime;

    /** client_ip */
    @Column(name = "client_ip", length = 50)
    private String clientIp;

    /** user_agent */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /** exception_msg */
    @Column(name = "exception_msg", columnDefinition = "text")
    private String exceptionMsg;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
