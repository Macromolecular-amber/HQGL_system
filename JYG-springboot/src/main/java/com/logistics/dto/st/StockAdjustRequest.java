package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 库存调整请求
 */
@Data
public class StockAdjustRequest {

    /** 物资ID（必填） */
    @NotNull(message = "物资不能为空")
    private Long materialId;

    /** 调整后库存（必填，≥0） */
    @NotNull(message = "调整后库存不能为空")
    @DecimalMin(value = "0", message = "调整后库存不能小于0")
    private BigDecimal newStock;

    /** 调整备注 */
    private String remark;
}
