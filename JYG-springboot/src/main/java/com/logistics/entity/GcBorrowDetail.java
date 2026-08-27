package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * gc_borrow_detail 实体
 */
@Data
@Entity
@Table(name = "gc_borrow_detail")
public class GcBorrowDetail {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** borrow_order_id */
    @Column(name = "borrow_order_id")
    private Long borrowOrderId;

    /** asset_id */
    @Column(name = "asset_id")
    private Long assetId;

    /** asset_code */
    @Column(name = "asset_code", length = 50)
    private String assetCode;

    /** asset_name */
    @Column(name = "asset_name", length = 200)
    private String assetName;

    /** borrow_quantity */
    @Column(name = "borrow_quantity")
    private Integer borrowQuantity;

    /** actual_quantity */
    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    /** return_quantity */
    @Column(name = "return_quantity")
    private Integer returnQuantity;

    /** detail_status */
    @Column(name = "detail_status", length = 20)
    private String detailStatus;

    /** return_time */
    @Column(name = "return_time")
    private OffsetDateTime returnTime;

    /** accept_status */
    @Column(name = "accept_status", length = 20)
    private String acceptStatus;

    /** accept_remark */
    @Column(name = "accept_remark", columnDefinition = "text")
    private String acceptRemark;

    /** damage_description */
    @Column(name = "damage_description", columnDefinition = "text")
    private String damageDescription;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

}
