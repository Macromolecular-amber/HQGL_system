package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 取消订餐请求
 */
@Data
public class MealCancelRequest {

    /** 预约ID（必填） */
    @NotNull(message = "预约ID不能为空")
    private Long reservationId;
}
