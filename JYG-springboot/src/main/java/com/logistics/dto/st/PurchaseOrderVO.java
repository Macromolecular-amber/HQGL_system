package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 采购单视图：包含 StPurchaseOrder 全部字段，并补充明细列表与状态中文名
 */
@Data
public class PurchaseOrderVO {

    /** id */
    private Long id;

    /** 采购单编号 */
    private String orderNo;

    /** 采购原因 */
    private String purchaseReason;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 物资种类数 */
    private Integer materialCount;

    /** 生效开始时间 */
    private OffsetDateTime effectiveStart;

    /** 生效结束时间 */
    private OffsetDateTime effectiveEnd;

    /** 是否过期 */
    private Boolean isExpired;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 单据状态 */
    private String orderStatus;

    /** 流程实例ID */
    private String processInstanceId;

    /** 验收人列表 */
    private String acceptUsers;

    /** 验收时间 */
    private OffsetDateTime acceptTime;

    /** 验收状态 */
    private String acceptStatus;

    /** 验收意见 */
    private String acceptRemark;

    /** 审批人ID */
    private Long auditUserId;

    /** 审批人姓名 */
    private String auditUserName;

    /** 审批时间 */
    private OffsetDateTime auditTime;

    /** 审批意见 */
    private String auditRemark;

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

    /** 采购明细 */
    private List<PurchaseDetailVO> items;

    /** 状态中文名 */
    private String statusLabel;
}
