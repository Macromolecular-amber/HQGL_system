package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * gc_borrow_order 实体
 */
@Data
@Entity
@Table(name = "gc_borrow_order")
public class GcBorrowOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** order_no */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** applicant_id */
    @Column(name = "applicant_id")
    private Long applicantId;

    /** applicant_name */
    @Column(name = "applicant_name", length = 50)
    private String applicantName;

    /** applicant_unit_id */
    @Column(name = "applicant_unit_id")
    private Long applicantUnitId;

    /** applicant_unit_name */
    @Column(name = "applicant_unit_name", length = 100)
    private String applicantUnitName;

    /** applicant_phone */
    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    /** borrow_start */
    @Column(name = "borrow_start")
    private OffsetDateTime borrowStart;

    /** borrow_end */
    @Column(name = "borrow_end")
    private OffsetDateTime borrowEnd;

    /** borrow_reason */
    @Column(name = "borrow_reason", columnDefinition = "text")
    private String borrowReason;

    /** borrow_purpose */
    @Column(name = "borrow_purpose", length = 50)
    private String borrowPurpose;

    /** remark */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    /** order_status */
    @Column(name = "order_status", length = 20)
    private String orderStatus;

    /** approval_status */
    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    /** current_approver_id */
    @Column(name = "current_approver_id")
    private Long currentApproverId;

    /** current_approver_node */
    @Column(name = "current_approver_node", length = 100)
    private String currentApproverNode;

    /** warehouse_out_time */
    @Column(name = "warehouse_out_time")
    private OffsetDateTime warehouseOutTime;

    /** warehouse_in_time */
    @Column(name = "warehouse_in_time")
    private OffsetDateTime warehouseInTime;

    /** logistics_order_no */
    @Column(name = "logistics_order_no", length = 50)
    private String logisticsOrderNo;

    /** logistics_status */
    @Column(name = "logistics_status", length = 20)
    private String logisticsStatus;

    /** extension_count */
    @Column(name = "extension_count")
    private Integer extensionCount;

    /** max_extension */
    @Column(name = "max_extension")
    private Integer maxExtension;

    /** process_instance_id */
    @Column(name = "process_instance_id", length = 50)
    private String processInstanceId;

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

}
