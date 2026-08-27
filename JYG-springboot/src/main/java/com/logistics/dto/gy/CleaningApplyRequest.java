package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 公寓保洁申请请求
 */
@Data
public class CleaningApplyRequest {

    /** 房间ID（必填） */
    @NotNull(message = "房间不能为空")
    private Long roomId;

    /** 保洁类型：REGULAR 定期 / ON_DEMAND 按需（必填） */
    @NotBlank(message = "保洁类型不能为空")
    private String cleaningType;

    /** 保洁日期（必填） */
    @NotNull(message = "保洁日期不能为空")
    private LocalDate cleaningDate;

    /** 保洁时段：MORNING 上午 / AFTERNOON 下午（选填） */
    private String cleaningTimeSlot;

    /** 保洁范围 */
    private String cleaningScope;

    /** 保洁要求 */
    private String cleaningRequirement;
}
