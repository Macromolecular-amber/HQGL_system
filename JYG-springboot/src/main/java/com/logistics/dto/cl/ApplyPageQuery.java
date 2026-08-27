package com.logistics.dto.cl;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 用车申请分页查询条件
 */
@Data
public class ApplyPageQuery {

    /** 申请编号（模糊） */
    private String applyNo;

    /** 申请状态 */
    private String applyStatus;

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
