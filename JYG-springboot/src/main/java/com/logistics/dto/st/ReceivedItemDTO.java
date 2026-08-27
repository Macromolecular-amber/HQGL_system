package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 验收明细项
 */
@Data
public class ReceivedItemDTO {

    /** 采购明细ID（必填） */
    @NotNull(message = "明细ID不能为空")
    private Long detailId;

    /** 实际验收数量（必填，≥0） */
    @NotNull(message = "验收数量不能为空")
    @DecimalMin(value = "0", message = "验收数量不能小于0")
    private BigDecimal receivedQuantity;
}
