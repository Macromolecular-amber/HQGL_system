package com.logistics.dto.st;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 备餐统计响应
 */
@Data
public class MealStatisticsVO {

    /** 用餐日期 */
    private LocalDate mealDate;

    /** 餐次：BREAKFAST / LUNCH / DINNER */
    private String mealType;

    /** 总预约人数 */
    private Integer totalCount;

    /** 按单位统计 */
    private List<UnitMealStatVO> unitStatistics;
}
