package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度采购统计项
 */
@Data
public class MonthlyPurchaseItem {

    /** 月份（yyyy-MM） */
    private String month;

    /** 采购金额 */
    private BigDecimal totalAmount;
}
