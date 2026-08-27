package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 餐余记录请求
 */
@Data
public class WasteRecordRequest {

    /** 记录日期（必填） */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    /** 餐次：BREAKFAST / LUNCH / DINNER（可选） */
    private String mealType;

    /** 餐余重量（必填，>0） */
    @NotNull(message = "餐余重量不能为空")
    @DecimalMin(value = "0.01", message = "餐余重量必须大于0")
    private BigDecimal wasteWeight;

    /** 餐余类型：FOOD 食物 / PACKAGING 包装 / OTHER 其他（可选） */
    private String wasteType;

    /** 处理方式：COMPOST 堆肥 / FEED 饲料 / WASTE 废弃物（可选） */
    private String disposalMethod;

    /** 处理人（可选） */
    private String disposalPerson;

    /** 备注 */
    private String remark;
}
