package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * st_purchase_order 实体
 */
@Data
@Entity
@Table(name = "st_purchase_order")
public class StPurchaseOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** order_no */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** purchase_reason */
    @Column(name = "purchase_reason", columnDefinition = "text")
    private String purchaseReason;

    /** total_amount */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    /** material_count */
    @Column(name = "material_count")
    private Integer materialCount;

    /** effective_start */
    @Column(name = "effective_start")
    private OffsetDateTime effectiveStart;

    /** effective_end */
    @Column(name = "effective_end")
    private OffsetDateTime effectiveEnd;

    /** is_expired */
    @Column(name = "is_expired")
    private Boolean isExpired;

    /** supplier_id */
    @Column(name = "supplier_id")
    private Long supplierId;

    /** supplier_name */
    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    /** order_status */
    @Column(name = "order_status", length = 20)
    private String orderStatus;

    /** process_instance_id */
    @Column(name = "process_instance_id", length = 50)
    private String processInstanceId;

    /** accept_users */
    @Column(name = "accept_users", columnDefinition = "text")
    private String acceptUsers;

    /** accept_time */
    @Column(name = "accept_time")
    private OffsetDateTime acceptTime;

    /** accept_status */
    @Column(name = "accept_status", length = 20)
    private String acceptStatus;

    /** 验收意见 */
    @Column(name = "accept_remark", columnDefinition = "text")
    private String acceptRemark;

    /** 审批人ID */
    @Column(name = "audit_user_id")
    private Long auditUserId;

    /** 审批人姓名 */
    @Column(name = "audit_user_name", length = 50)
    private String auditUserName;

    /** 审批时间 */
    @Column(name = "audit_time")
    private OffsetDateTime auditTime;

    /** 审批意见 */
    @Column(name = "audit_remark", columnDefinition = "text")
    private String auditRemark;

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
