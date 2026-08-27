package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 备餐统计查询
 */
@Data
public class MealStatisticsQuery {

    /** 用餐日期（必填） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate mealDate;

    /** 餐次（可选，为空则统计全部餐次） */
    private String mealType;
}
