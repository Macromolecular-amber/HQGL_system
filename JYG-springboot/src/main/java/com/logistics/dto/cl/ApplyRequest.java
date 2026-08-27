package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 用车申请请求
 */
@Data
public class ApplyRequest {

    /** 用车事由 */
    @NotBlank(message = "用车事由不能为空")
    private String purpose;

    /** 目的地 */
    @NotBlank(message = "目的地不能为空")
    private String destination;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 乘车人数 */
    @NotNull(message = "乘车人数不能为空")
    @Min(value = 1, message = "乘车人数至少1人")
    private Integer passengerCount;

    /** 所需车型：SEDAN / SUV / MPV / BUS */
    @NotBlank(message = "所需车型不能为空")
    private String requiredVehicleType;

    /** 备注 */
    private String remark;
}
