package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物资采购统计项
 */
@Data
public class MaterialStatItem {

    /** 物资ID */
    private Long materialId;

    /** 物资名称 */
    private String materialName;

    /** 采购金额 */
    private BigDecimal totalAmount;
}
