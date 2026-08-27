package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 餐余统计趋势项
 */
@Data
public class WasteTrendItem {

    /** 日期 */
    private LocalDate date;

    /** 当日餐余总量 */
    private BigDecimal totalWeight;
}
