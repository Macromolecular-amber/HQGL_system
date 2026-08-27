package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 食堂物资视图：包含 StMaterial 全部字段，并补充分类中文名
 */
@Data
public class MaterialVO {

    /** id */
    private Long id;

    /** 物资编码 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 分类：FRESH_INGREDIENTS 生鲜食材 / CONDIMENT 调味品 / DAILY_GOODS 日用品 */
    private String category;

    /** 规格 */
    private String spec;

    /** 计量单位 */
    private String unit;

    /** 保质期（天） */
    private Integer shelfLife;

    /** 安全库存 */
    private BigDecimal safetyStock;

    /** 库存上限 */
    private BigDecimal maxStock;

    /** 当前单价 */
    private BigDecimal currentPrice;

    /** 上次单价 */
    private BigDecimal lastPrice;

    /** 当前库存 */
    private BigDecimal currentStock;

    /** 已占用库存 */
    private BigDecimal occupiedStock;

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

    /** 分类中文名 */
    private String categoryLabel;
}
