package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 食堂预约订餐请求
 */
@Data
public class MealReserveRequest {

    /** 用餐日期（必填） */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate mealDate;

    /** 餐次（必填）：BREAKFAST 早餐 / LUNCH 午餐 / DINNER 晚餐 */
    private String mealType;

    /** 预约人数，默认 1 */
    private Integer mealCount = 1;

    /** 备注 */
    private String remark;
}
