package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 餐余记录查询条件
 */
@Data
public class WastePageQuery {

    /** 起始日期 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDateStart;

    /** 结束日期 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDateEnd;

    /** 餐次 */
    private String mealType;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
