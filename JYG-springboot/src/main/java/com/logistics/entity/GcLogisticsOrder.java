package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * gc_logistics_order 实体
 */
@Data
@Entity
@Table(name = "gc_logistics_order")
public class GcLogisticsOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** logistics_no */
    @Column(name = "logistics_no", length = 50)
    private String logisticsNo;

    /** business_type */
    @Column(name = "business_type", length = 20)
    private String businessType;

    /** business_order_no */
    @Column(name = "business_order_no", length = 50)
    private String businessOrderNo;

    /** delivery_method */
    @Column(name = "delivery_method", length = 20)
    private String deliveryMethod;

    /** sender_name */
    @Column(name = "sender_name", length = 50)
    private String senderName;

    /** sender_phone */
    @Column(name = "sender_phone", length = 20)
    private String senderPhone;

    /** sender_address */
    @Column(name = "sender_address", length = 200)
    private String senderAddress;

    /** receiver_name */
    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    /** receiver_phone */
    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    /** receiver_address */
    @Column(name = "receiver_address", length = 200)
    private String receiverAddress;

    /** cargo_desc */
    @Column(name = "cargo_desc", columnDefinition = "text")
    private String cargoDesc;

    /** cargo_weight */
    @Column(name = "cargo_weight")
    private BigDecimal cargoWeight;

    /** cargo_volume */
    @Column(name = "cargo_volume")
    private BigDecimal cargoVolume;

    /** logistics_status */
    @Column(name = "logistics_status", length = 20)
    private String logisticsStatus;

    /** pickup_time */
    @Column(name = "pickup_time")
    private OffsetDateTime pickupTime;

    /** delivery_time */
    @Column(name = "delivery_time")
    private OffsetDateTime deliveryTime;

    /** sign_time */
    @Column(name = "sign_time")
    private OffsetDateTime signTime;

    /** sign_person */
    @Column(name = "sign_person", length = 50)
    private String signPerson;

    /** sign_photo_url */
    @Column(name = "sign_photo_url", length = 255)
    private String signPhotoUrl;

    /** third_party_tracking_no */
    @Column(name = "third_party_tracking_no", length = 50)
    private String thirdPartyTrackingNo;

    /** third_party_platform */
    @Column(name = "third_party_platform", length = 50)
    private String thirdPartyPlatform;

    /** freight_amount */
    @Column(name = "freight_amount")
    private BigDecimal freightAmount;

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

}
