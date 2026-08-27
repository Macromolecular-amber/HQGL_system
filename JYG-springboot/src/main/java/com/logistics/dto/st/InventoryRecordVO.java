package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 库存流水视图：包含 StInventoryRecord 全部字段，并补充类型中文名与物资信息
 */
@Data
public class InventoryRecordVO {

    /** id */
    private Long id;

    /** 物资ID */
    private Long materialId;

    /** 物资编码 */
    private String materialCode;

    /** 物资名称 */
    private String materialName;

    /** 流水类型：IN / OUT / ADJUST / LOSS */
    private String recordType;

    /** 数量（入库为正，出库为负，调整按差值） */
    private BigDecimal quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 业务单号（如采购单号） */
    private String businessOrderNo;

    /** 业务类型：PURCHASE 采购 / CONSUME 领用 */
    private String businessType;

    /** 变动前库存 */
    private BigDecimal stockBefore;

    /** 变动后库存 */
    private BigDecimal stockAfter;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 流水类型中文名 */
    private String recordTypeLabel;

    /** 物资规格 */
    private String spec;

    /** 计量单位 */
    private String unit;
}
