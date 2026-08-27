package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.ColumnTransformer;

/**
 * gy_occupant 实体
 */
@Data
@Entity
@Table(name = "gy_occupant")
public class GyOccupant {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** occupant_name */
    @Column(name = "occupant_name", length = 50)
    private String occupantName;

    /** occupant_type */
    @Column(name = "occupant_type", length = 20)
    private String occupantType;

    /** id_card */
    @Column(name = "id_card", length = 18)
    private String idCard;

    /** phone */
    @Column(name = "phone", length = 20)
    private String phone;

    /** unit_id */
    @Column(name = "unit_id")
    private Long unitId;

    /** unit_name */
    @Column(name = "unit_name", length = 100)
    private String unitName;

    /** position */
    @Column(name = "position", length = 50)
    private String position;

    /** attendant_info */
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "attendant_info", columnDefinition = "jsonb")
    private String attendantInfo;

    /** room_id */
    @Column(name = "room_id")
    private Long roomId;

    /** room_no */
    @Column(name = "room_no", length = 20)
    private String roomNo;

    /** checkin_time */
    @Column(name = "checkin_time")
    private OffsetDateTime checkinTime;

    /** expected_leave_time */
    @Column(name = "expected_leave_time")
    private OffsetDateTime expectedLeaveTime;

    /** actual_leave_time */
    @Column(name = "actual_leave_time")
    private OffsetDateTime actualLeaveTime;

    /** rent_amount */
    @Column(name = "rent_amount")
    private BigDecimal rentAmount;

    /** rent_paid_status */
    @Column(name = "rent_paid_status", length = 20)
    private String rentPaidStatus;

    /** assign_method */
    @Column(name = "assign_method", length = 20)
    private String assignMethod;

    /** approval_file_url */
    @Column(name = "approval_file_url", length = 255)
    private String approvalFileUrl;

    /** occupant_status */
    @Column(name = "occupant_status", length = 20)
    private String occupantStatus;

    /** apply_id */
    @Column(name = "apply_id")
    private Long applyId;

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

    /** 申请原因（人才公寓申请） */
    @Column(name = "apply_reason", columnDefinition = "text")
    private String applyReason;

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

    /** 备注 */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    /** 退住时间 */
    @Column(name = "checkout_time")
    private OffsetDateTime checkoutTime;

    /** 房屋状况 */
    @Column(name = "room_condition", columnDefinition = "text")
    private String roomCondition;

    /** 资产核对结果（设施名称 -> 完好/损坏） */
    @Column(name = "facility_check_result", columnDefinition = "text")
    private String facilityCheckResult;

    /** 结算金额 */
    @Column(name = "settlement_amount")
    private BigDecimal settlementAmount;

    /** 结算明细 */
    @Column(name = "settlement_detail", columnDefinition = "text")
    private String settlementDetail;

    /** 退房照片 */
    @Column(name = "checkout_photos", columnDefinition = "text")
    private String checkoutPhotos;

    /** 验收人ID */
    @Column(name = "accept_user_id")
    private Long acceptUserId;

    /** 验收人姓名 */
    @Column(name = "accept_user_name", length = 50)
    private String acceptUserName;

    /** 验收时间 */
    @Column(name = "accept_time")
    private OffsetDateTime acceptTime;

}
