package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 采购单分页查询条件
 */
@Data
public class PurchasePageQuery {

    /** 采购单编号（模糊） */
    private String orderNo;

    /** 单据状态 */
    private String orderStatus;

    /** 供应商名称（模糊） */
    private String supplierName;

    /** 起始日期（按创建时间） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（按创建时间） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
