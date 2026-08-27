package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.ColumnTransformer;

/**
 * cl_repair_order 实体
 */
@Data
@Entity
@Table(name = "cl_repair_order")
public class ClRepairOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** repair_no */
    @Column(name = "repair_no", length = 50)
    private String repairNo;

    /** vehicle_id */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** plate_number */
    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    /** repair_type */
    @Column(name = "repair_type", length = 20)
    private String repairType;

    /** fault_desc */
    @Column(name = "fault_desc", columnDefinition = "text")
    private String faultDesc;

    /** fault_photos */
    @Column(name = "fault_photos", columnDefinition = "text")
    private String faultPhotos;

    /** urgency_level */
    @Column(name = "urgency_level", length = 20)
    private String urgencyLevel;

    /** repair_shop_id */
    @Column(name = "repair_shop_id")
    private Long repairShopId;

    /** repair_shop_name */
    @Column(name = "repair_shop_name", length = 100)
    private String repairShopName;

    /** estimated_cost */
    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    /** actual_cost */
    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    /** parts_detail */
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "parts_detail", columnDefinition = "jsonb")
    private String partsDetail;

    /** labor_cost */
    @Column(name = "labor_cost")
    private BigDecimal laborCost;

    /** order_status */
    @Column(name = "order_status", length = 20)
    private String orderStatus;

    /** process_instance_id */
    @Column(name = "process_instance_id", length = 50)
    private String processInstanceId;

    /** repair_start */
    @Column(name = "repair_start")
    private OffsetDateTime repairStart;

    /** repair_end */
    @Column(name = "repair_end")
    private OffsetDateTime repairEnd;

    /** repair_photos */
    @Column(name = "repair_photos", columnDefinition = "text")
    private String repairPhotos;

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

    /** repair_mileage */
    @Column(name = "repair_mileage")
    private BigDecimal repairMileage;

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
