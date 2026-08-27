package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * gc_asset_card 实体
 */
@Data
@Entity
@Table(name = "gc_asset_card")
public class GcAssetCard {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** asset_code */
    @Column(name = "asset_code", length = 50)
    private String assetCode;

    /** asset_name */
    @Column(name = "asset_name", length = 200)
    private String assetName;

    /** category_code */
    @Column(name = "category_code", length = 20)
    private String categoryCode;

    /** category_name */
    @Column(name = "category_name", length = 50)
    private String categoryName;

    /** spec_model */
    @Column(name = "spec_model", length = 100)
    private String specModel;

    /** brand */
    @Column(name = "brand", length = 50)
    private String brand;

    /** quantity */
    @Column(name = "quantity")
    private Integer quantity;

    /** original_value */
    @Column(name = "original_value")
    private BigDecimal originalValue;

    /** residual_rate */
    @Column(name = "residual_rate")
    private BigDecimal residualRate;

    /** current_value */
    @Column(name = "current_value")
    private BigDecimal currentValue;

    /** accumulated_depreciation */
    @Column(name = "accumulated_depreciation")
    private BigDecimal accumulatedDepreciation;

    /** purchase_date */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /** useful_life */
    @Column(name = "useful_life")
    private Integer usefulLife;

    /** depreciation_method */
    @Column(name = "depreciation_method", length = 20)
    private String depreciationMethod;

    /** asset_status */
    @Column(name = "asset_status", length = 20)
    private String assetStatus;

    /** location */
    @Column(name = "location", length = 200)
    private String location;

    /** warehouse_id */
    @Column(name = "warehouse_id")
    private Long warehouseId;

    /** owner_unit_id */
    @Column(name = "owner_unit_id")
    private Long ownerUnitId;

    /** owner_unit_name */
    @Column(name = "owner_unit_name", length = 100)
    private String ownerUnitName;

    /** current_use_unit_id */
    @Column(name = "current_use_unit_id")
    private Long currentUseUnitId;

    /** qr_code_url */
    @Column(name = "qr_code_url", length = 255)
    private String qrCodeUrl;

    /** rfid_tag */
    @Column(name = "rfid_tag", length = 50)
    private String rfidTag;

    /** photo_urls */
    @Column(name = "photo_urls", columnDefinition = "text")
    private String photoUrls;

    /** attachment_urls */
    @Column(name = "attachment_urls", columnDefinition = "text")
    private String attachmentUrls;

    /** description */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** remark */
    @Column(name = "remark", length = 500)
    private String remark;

    /** create_by */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_by */
    @Column(name = "update_by")
    private Long updateBy;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    /** is_deleted */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

    /** audit_user_id */
    @Column(name = "audit_user_id")
    private Long auditUserId;

    /** audit_user_name */
    @Column(name = "audit_user_name", length = 50)
    private String auditUserName;

    /** audit_time */
    @Column(name = "audit_time")
    private OffsetDateTime auditTime;

    /** audit_remark */
    @Column(name = "audit_remark", columnDefinition = "text")
    private String auditRemark;

    /** in_stock_time */
    @Column(name = "in_stock_time")
    private OffsetDateTime inStockTime;

}
