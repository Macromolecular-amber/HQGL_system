package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * st_purchase_detail 实体
 */
@Data
@Entity
@Table(name = "st_purchase_detail")
public class StPurchaseDetail {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** purchase_order_id */
    @Column(name = "purchase_order_id")
    private Long purchaseOrderId;

    /** material_id */
    @Column(name = "material_id")
    private Long materialId;

    /** material_code */
    @Column(name = "material_code", length = 50)
    private String materialCode;

    /** material_name */
    @Column(name = "material_name", length = 100)
    private String materialName;

    /** quantity */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /** unit_price */
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    /** subtotal */
    @Column(name = "subtotal")
    private BigDecimal subtotal;

    /** received_quantity */
    @Column(name = "received_quantity")
    private BigDecimal receivedQuantity;

    /** receive_time */
    @Column(name = "receive_time")
    private OffsetDateTime receiveTime;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

}
