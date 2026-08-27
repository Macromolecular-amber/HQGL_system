package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * gc_return_order 实体
 */
@Data
@Entity
@Table(name = "gc_return_order")
public class GcReturnOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** return_no */
    @Column(name = "return_no", length = 50)
    private String returnNo;

    /** borrow_order_id */
    @Column(name = "borrow_order_id")
    private Long borrowOrderId;

    /** return_applicant_id */
    @Column(name = "return_applicant_id")
    private Long returnApplicantId;

    /** return_applicant_unit_id */
    @Column(name = "return_applicant_unit_id")
    private Long returnApplicantUnitId;

    /** plan_return_time */
    @Column(name = "plan_return_time")
    private OffsetDateTime planReturnTime;

    /** actual_return_time */
    @Column(name = "actual_return_time")
    private OffsetDateTime actualReturnTime;

    /** accept_user_ids */
    @Column(name = "accept_user_ids", columnDefinition = "text")
    private String acceptUserIds;

    /** accept_time */
    @Column(name = "accept_time")
    private OffsetDateTime acceptTime;

    /** accept_result */
    @Column(name = "accept_result", length = 20)
    private String acceptResult;

    /** accept_remark */
    @Column(name = "accept_remark", columnDefinition = "text")
    private String acceptRemark;

    /** accept_photos */
    @Column(name = "accept_photos", columnDefinition = "text")
    private String acceptPhotos;

    /** damage_info */
    @Column(name = "damage_info", columnDefinition = "text")
    private String damageInfo;

    /** damage_responsibility */
    @Column(name = "damage_responsibility", length = 50)
    private String damageResponsibility;

    /** repair_cost */
    @Column(name = "repair_cost")
    private BigDecimal repairCost;

    /** compensation_amount */
    @Column(name = "compensation_amount")
    private BigDecimal compensationAmount;

    /** return_status */
    @Column(name = "return_status", length = 20)
    private String returnStatus;

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
