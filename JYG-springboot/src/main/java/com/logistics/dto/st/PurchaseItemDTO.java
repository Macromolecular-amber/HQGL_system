package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 采购项
 */
@Data
public class PurchaseItemDTO {

    /** 物资ID（必填） */
    @NotNull(message = "物资不能为空")
    private Long materialId;

    /** 采购数量（必填，>0） */
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.01", message = "采购数量必须大于0")
    private BigDecimal quantity;

    /** 单价（必填，≥0） */
    @NotNull(message = "采购单价不能为空")
    @DecimalMin(value = "0", message = "采购单价不能小于0")
    private BigDecimal unitPrice;
}
