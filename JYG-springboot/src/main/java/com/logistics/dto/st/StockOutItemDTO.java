package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 出库项
 */
@Data
public class StockOutItemDTO {

    /** 物资ID（必填） */
    @NotNull(message = "物资不能为空")
    private Long materialId;

    /** 出库数量（必填，>0） */
    @NotNull(message = "出库数量不能为空")
    @DecimalMin(value = "0.01", message = "出库数量必须大于0")
    private BigDecimal quantity;

    /** 出库单价（可选） */
    @DecimalMin(value = "0", message = "出库单价不能小于0")
    private BigDecimal unitPrice;
}
