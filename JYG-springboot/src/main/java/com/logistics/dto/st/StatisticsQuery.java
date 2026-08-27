package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 统计分析查询
 */
@Data
public class StatisticsQuery {

    /** 统计周期：DAY 日 / WEEK 周 / MONTH 月 / YEAR 年 */
    private String periodType;

    /** 起始日期（必填） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 结束日期（必填） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}
