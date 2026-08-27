package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购统计响应
 */
@Data
public class PurchaseStatisticsVO {

    /** 采购总金额 */
    private BigDecimal totalPurchaseAmount;

    /** 采购金额前5的物资 */
    private List<MaterialStatItem> topMaterials;

    /** 月度采购趋势 */
    private List<MonthlyPurchaseItem> monthlyTrend;
}
