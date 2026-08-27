package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 餐余统计响应
 */
@Data
public class WasteStatisticsVO {

    /** 总餐余重量 */
    private BigDecimal totalWeight;

    /** 日均餐余重量 */
    private BigDecimal avgWeightPerDay;

    /** 趋势数据（按日） */
    private List<WasteTrendItem> trendData;

    /** 按餐次统计 */
    private Map<String, BigDecimal> byMealType;
}
