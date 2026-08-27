package com.logistics.dto.st;

import lombok.Data;

/**
 * 单位备餐统计项
 */
@Data
public class UnitMealStatVO {

    /** 单位ID */
    private Long unitId;

    /** 单位名称 */
    private String unitName;

    /** 预约人数 */
    private Integer count;
}
