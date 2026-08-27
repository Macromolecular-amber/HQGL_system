package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * gy_cleaning_order 实体
 */
@Data
@Entity
@Table(name = "gy_cleaning_order")
public class GyCleaningOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** cleaning_no */
    @Column(name = "cleaning_no", length = 50)
    private String cleaningNo;

    /** room_id */
    @Column(name = "room_id")
    private Long roomId;

    /** room_no */
    @Column(name = "room_no", length = 20)
    private String roomNo;

    /** applicant_id */
    @Column(name = "applicant_id")
    private Long applicantId;

    /** applicant_name */
    @Column(name = "applicant_name", length = 50)
    private String applicantName;

    /** cleaning_time */
    @Column(name = "cleaning_time")
    private OffsetDateTime cleaningTime;

    /** cleaning_scope */
    @Column(name = "cleaning_scope", columnDefinition = "text")
    private String cleaningScope;

    /** cleaning_requirement */
    @Column(name = "cleaning_requirement", columnDefinition = "text")
    private String cleaningRequirement;

    /** order_status */
    @Column(name = "order_status", length = 20)
    private String orderStatus;

    /** assignee_id */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /** assignee_name */
    @Column(name = "assignee_name", length = 50)
    private String assigneeName;

    /** assignee_company */
    @Column(name = "assignee_company", length = 100)
    private String assigneeCompany;

    /** assign_time */
    @Column(name = "assign_time")
    private OffsetDateTime assignTime;

    /** execute_time */
    @Column(name = "execute_time")
    private OffsetDateTime executeTime;

    /** execute_photos */
    @Column(name = "execute_photos", columnDefinition = "text")
    private String executePhotos;

    /** accept_user_id */
    @Column(name = "accept_user_id")
    private Long acceptUserId;

    /** accept_time */
    @Column(name = "accept_time")
    private OffsetDateTime acceptTime;

    /** accept_result */
    @Column(name = "accept_result", length = 20)
    private String acceptResult;

    /** accept_remark */
    @Column(name = "accept_remark", columnDefinition = "text")
    private String acceptRemark;

    /** accept_score */
    @Column(name = "accept_score")
    private Integer acceptScore;

    /** settle_amount */
    @Column(name = "settle_amount")
    private BigDecimal settleAmount;

    /** settle_status */
    @Column(name = "settle_status", length = 20)
    private String settleStatus;

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

    /** 保洁类型：REGULAR 定期 / ON_DEMAND 按需 */
    @Column(name = "cleaning_type", length = 20)
    private String cleaningType;

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

}
