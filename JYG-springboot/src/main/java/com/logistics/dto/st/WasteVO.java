package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 餐余记录视图：包含 StKitchenWaste 全部字段，并补充中文名
 */
@Data
public class WasteVO {

    /** id */
    private Long id;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 餐次 */
    private String mealType;

    /** 餐余重量 */
    private BigDecimal wasteWeight;

    /** 餐余类型 */
    private String wasteType;

    /** 处理方式 */
    private String disposalMethod;

    /** 处理人 */
    private String disposalPerson;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 逻辑删除 */
    private Boolean isDeleted;

    /** 餐次中文名 */
    private String mealTypeLabel;

    /** 餐余类型中文名 */
    private String wasteTypeLabel;

    /** 处理方式中文名 */
    private String disposalMethodLabel;
}
