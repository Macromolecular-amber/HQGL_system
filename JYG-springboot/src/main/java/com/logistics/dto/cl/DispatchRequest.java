package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 派单请求
 */
@Data
public class DispatchRequest {

    /** 关联用车申请ID */
    @NotNull(message = "用车申请不能为空")
    private Long applyId;

    /** 车辆ID */
    @NotNull(message = "车辆不能为空")
    private Long vehicleId;

    /** 驾驶员ID */
    @NotNull(message = "驾驶员不能为空")
    private Long driverId;

    /** 计划开始时间 */
    @NotNull(message = "计划开始时间不能为空")
    private LocalDateTime scheduledStart;

    /** 计划结束时间 */
    @NotNull(message = "计划结束时间不能为空")
    private LocalDateTime scheduledEnd;

    /** 备注 */
    private String remark;
}
