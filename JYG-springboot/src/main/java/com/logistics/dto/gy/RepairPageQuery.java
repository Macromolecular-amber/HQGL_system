package com.logistics.dto.gy;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 公寓维修单分页查询条件
 */
@Data
public class RepairPageQuery {

    /** 维修单号（模糊） */
    private String repairNo;

    /** 房间ID */
    private Long roomId;

    /** 维修类型 */
    private String repairType;

    /** 费用承担方式 */
    private String costType;

    /** 单据状态 */
    private String orderStatus;

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
