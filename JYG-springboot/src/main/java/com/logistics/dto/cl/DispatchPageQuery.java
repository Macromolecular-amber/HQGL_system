package com.logistics.dto.cl;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 调度单分页查询条件
 */
@Data
public class DispatchPageQuery {

    /** 派单编号（模糊） */
    private String dispatchNo;

    /** 调度状态 */
    private String dispatchStatus;

    /** 车辆ID */
    private Long vehicleId;

    /** 驾驶员ID */
    private Long driverId;

    /** 创建时间起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 创建时间止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
