package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * cl_apply_order 实体
 */
@Data
@Entity
@Table(name = "cl_apply_order")
public class ClApplyOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** apply_no */
    @Column(name = "apply_no", length = 50)
    private String applyNo;

    /** applicant_id */
    @Column(name = "applicant_id")
    private Long applicantId;

    /** applicant_name */
    @Column(name = "applicant_name", length = 50)
    private String applicantName;

    /** applicant_unit_id */
    @Column(name = "applicant_unit_id")
    private Long applicantUnitId;

    /** applicant_phone */
    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    /** purpose */
    @Column(name = "purpose", length = 200)
    private String purpose;

    /** destination */
    @Column(name = "destination", length = 200)
    private String destination;

    /** remark */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    /** start_time */
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    /** end_time */
    @Column(name = "end_time")
    private OffsetDateTime endTime;

    /** passenger_count */
    @Column(name = "passenger_count")
    private Integer passengerCount;

    /** required_vehicle_type */
    @Column(name = "required_vehicle_type", length = 20)
    private String requiredVehicleType;

    /** planned_route */
    @Column(name = "planned_route", columnDefinition = "text")
    private String plannedRoute;

    /** planned_mileage */
    @Column(name = "planned_mileage")
    private BigDecimal plannedMileage;

    /** apply_status */
    @Column(name = "apply_status", length = 20)
    private String applyStatus;

    /** auto_approve */
    @Column(name = "auto_approve")
    private Boolean autoApprove;

    /** reject_reason */
    @Column(name = "reject_reason", columnDefinition = "text")
    private String rejectReason;

    /** dispatch_order_id */
    @Column(name = "dispatch_order_id")
    private Long dispatchOrderId;

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
