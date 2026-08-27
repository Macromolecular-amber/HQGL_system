package com.logistics.dto.cl;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 费用分页查询条件
 */
@Data
public class CostPageQuery {

    /** 车辆ID */
    private Long vehicleId;

    /** 费用类型 */
    private String costType;

    /** 审批状态 */
    private String approvalStatus;

    /** 费用时间起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    /** 费用时间止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
