package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * st_inventory_record 实体
 */
@Data
@Entity
@Table(name = "st_inventory_record")
public class StInventoryRecord {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** material_id */
    @Column(name = "material_id")
    private Long materialId;

    /** material_code */
    @Column(name = "material_code", length = 50)
    private String materialCode;

    /** material_name */
    @Column(name = "material_name", length = 100)
    private String materialName;

    /** record_type */
    @Column(name = "record_type", length = 20)
    private String recordType;

    /** quantity */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** unit_price */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** total_amount */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    /** business_order_no */
    @Column(name = "business_order_no", length = 50)
    private String businessOrderNo;

    /** business_type */
    @Column(name = "business_type", length = 30)
    private String businessType;

    /** stock_before */
    @Column(name = "stock_before")
    private BigDecimal stockBefore;

    /** stock_after */
    @Column(name = "stock_after")
    private BigDecimal stockAfter;

    /** operator_id */
    @Column(name = "operator_id")
    private Long operatorId;

    /** operator_name */
    @Column(name = "operator_name", length = 50)
    private String operatorName;

    /** remark */
    @Column(name = "remark", length = 500)
    private String remark;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
