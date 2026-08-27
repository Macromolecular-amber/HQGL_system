package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 采购明细视图：包含 StPurchaseDetail 全部字段，并补充物资规格/单位
 */
@Data
public class PurchaseDetailVO {

    /** id */
    private Long id;

    /** 采购单ID */
    private Long purchaseOrderId;

    /** 物资ID */
    private Long materialId;

    /** 物资编码 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 采购数量 */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 小计 */
    private BigDecimal subtotal;

    /** 实际验收数量 */
    private BigDecimal receivedQuantity;

    /** 验收时间 */
    private OffsetDateTime receiveTime;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 物资规格 */
    private String spec;

    /** 计量单位 */
    private String unit;
}
