package com.logistics.dto.st;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 订餐预约视图：包含 StMealReservation 全部字段，并补充餐次中文名
 */
@Data
public class MealReservationVO {

    /** id */
    private Long id;

    /** 预约人ID */
    private Long userId;

    /** 预约人姓名 */
    private String userName;

    /** 所属单位ID */
    private Long unitId;

    /** 所属单位名称 */
    private String unitName;

    /** 用餐日期 */
    private LocalDate mealDate;

    /** 餐次：BREAKFAST / LUNCH / DINNER */
    private String mealType;

    /** 预约人数 */
    private Integer mealCount;

    /** 预约时间 */
    private OffsetDateTime reservationTime;

    /** 取消时间 */
    private OffsetDateTime cancelTime;

    /** 是否已取消 */
    private Boolean isCancelled;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 餐次中文名 */
    private String mealTypeLabel;
}
