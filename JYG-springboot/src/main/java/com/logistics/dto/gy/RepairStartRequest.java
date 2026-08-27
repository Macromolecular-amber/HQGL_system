package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 开始维修请求
 */
@Data
public class RepairStartRequest {

    /** 维修单ID（必填） */
    @NotNull(message = "维修单ID不能为空")
    private Long repairId;

    /** 预估费用 */
    private BigDecimal estimatedCost;
}
