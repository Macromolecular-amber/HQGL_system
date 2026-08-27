package com.logistics.dto.gc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 资产详情视图：继承列表视图并补充完整字段与审核信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetDetailVO extends AssetListVO {

    /** 品牌 */
    private String brand;

    /** 数量 */
    private Integer quantity;

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

    /** 仓库ID */
    private Long warehouseId;

    /** 所属单位ID */
    private Long ownerUnitId;

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

    /** 更新人 */
    private Long updateBy;

    /** 逻辑删除 */
    private Boolean isDeleted;

    /** 审核人 */
    private String auditUser;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    private OffsetDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 入仓时间 */
    private OffsetDateTime inStockTime;

    /** 单位名称（冗余展示） */
    private String unitName;
}
