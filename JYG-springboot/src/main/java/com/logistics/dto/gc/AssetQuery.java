package com.logistics.dto.gc;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产列表查询条件
 */
@Data
public class AssetQuery {

    /** 资产名称（模糊） */
    private String assetName;

    /** 资产分类编码 */
    private String categoryCode;

    /** 资产状态 */
    private String assetStatus;

    /** 所属单位ID */
    private Long ownerUnitId;

    /** 原值下限 */
    private BigDecimal startValue;

    /** 原值上限 */
    private BigDecimal endValue;

    /** 购置日期起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDateStart;

    /** 购置日期止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDateEnd;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
