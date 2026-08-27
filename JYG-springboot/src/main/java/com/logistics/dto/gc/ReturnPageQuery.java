package com.logistics.dto.gc;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 归还单分页查询条件
 */
@Data
public class ReturnPageQuery {

    /** 归还单编号（模糊） */
    private String returnNo;

    /** 借用单ID */
    private Long borrowOrderId;

    /** 归还单状态 */
    private String returnStatus;

    /** 申请时间起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 申请时间止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
