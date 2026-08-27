package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * gc_transfer_detail 实体：调剂/处置资产明细
 */
@Data
@Entity
@Table(name = "gc_transfer_detail")
public class GcTransferDetail {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** transfer_order_id */
    @Column(name = "transfer_order_id")
    private Long transferOrderId;

    /** asset_id */
    @Column(name = "asset_id")
    private Long assetId;

    /** asset_code */
    @Column(name = "asset_code", length = 50)
    private String assetCode;

    /** asset_name */
    @Column(name = "asset_name", length = 200)
    private String assetName;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
