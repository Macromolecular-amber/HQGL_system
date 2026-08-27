package com.logistics.dto.gc;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 资产列表展示视图
 */
@Data
public class AssetListVO {

    /** id */
    private Long id;

    /** 资产编号 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 资产分类编码 */
    private String categoryCode;

    /** 资产分类名称 */
    private String categoryName;

    /** 规格型号 */
    private String specModel;

    /** 原值 */
    private BigDecimal originalValue;

    /** 资产状态 */
    private String assetStatus;

    /** 存放地点 */
    private String location;

    /** 所属单位名称 */
    private String ownerUnitName;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新时间 */
    private OffsetDateTime updateTime;
}
