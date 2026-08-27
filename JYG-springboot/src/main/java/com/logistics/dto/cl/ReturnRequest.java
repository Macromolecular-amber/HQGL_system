package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆归还请求
 */
@Data
public class ReturnRequest {

    /** 调度单ID */
    @NotNull(message = "调度单ID不能为空")
    private Long dispatchId;

    /** 实际结束时间 */
    @NotNull(message = "实际结束时间不能为空")
    private LocalDateTime actualEnd;

    /** 实际行驶里程 */
    @NotNull(message = "实际行驶里程不能为空")
    private BigDecimal actualMileage;

    /** 备注 */
    private String remark;
}
