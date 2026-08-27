package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 轨迹查询条件
 */
@Data
public class TrackQuery {

    /** 车辆ID（必填） */
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;

    /** 开始时间（必填） */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间（必填） */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
}
