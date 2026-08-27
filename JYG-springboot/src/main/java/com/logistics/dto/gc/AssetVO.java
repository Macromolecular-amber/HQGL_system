package com.logistics.dto.gc;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 资产视图对象：包含 GcAssetCard 全部字段，并补充单位名称、分类名称
 */
@Data
public class AssetVO {

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

    /** 品牌 */
    private String brand;

    /** 数量 */
    private Integer quantity;

    /** 原值 */
    private BigDecimal originalValue;

    /** 残值率(%) */
    private BigDecimal residualRate;

    /** 当前净值 */
    private BigDecimal currentValue;

    /** 累计折旧 */
    private BigDecimal accumulatedDepreciation;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /** 使用年限（年） */
    private Integer usefulLife;

    /** 折旧方法 */
    private String depreciationMethod;

    /** 资产状态 */
    private String assetStatus;

    /** 存放地点 */
    private String location;

    /** 仓库ID */
    private Long warehouseId;

    /** 所属单位ID */
    private Long ownerUnitId;

    /** 所属单位名称 */
    private String ownerUnitName;

    /** 当前使用单位ID */
    private Long currentUseUnitId;

    /** 二维码URL */
    private String qrCodeUrl;

    /** RFID标签 */
    private String rfidTag;

    /** 图片URL */
    private String photoUrls;

    /** 附件URL */
    private String attachmentUrls;

    /** 资产描述 */
    private String description;

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

    /** 审核人ID */
    private Long auditUserId;

    /** 审核人姓名 */
    private String auditUserName;

    /** 审核时间 */
    private OffsetDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 入仓时间 */
    private OffsetDateTime inStockTime;

    /** 单位名称（冗余展示，来自所属单位） */
    private String unitName;
}
