package com.logistics.dto.gc;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 处置单分页查询条件
 */
@Data
public class DisposePageQuery {

    /** 处置单编号（模糊） */
    private String orderNo;

    /** 处置单状态 */
    private String status;

    /** 处置方式 */
    private String disposeMethod;

    /** 申请单位ID */
    private Long applicantUnitId;

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
