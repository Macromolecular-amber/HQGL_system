package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * cl_cost_detail 实体
 */
@Data
@Entity
@Table(name = "cl_cost_detail")
public class ClCostDetail {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** vehicle_id */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** cost_type */
    @Column(name = "cost_type", length = 30)
    private String costType;

    /** cost_amount */
    @Column(name = "cost_amount")
    private BigDecimal costAmount;

    /** cost_time */
    @Column(name = "cost_time")
    private OffsetDateTime costTime;

    /** cost_desc */
    @Column(name = "cost_desc", columnDefinition = "text")
    private String costDesc;

    /** biz_order_no */
    @Column(name = "biz_order_no", length = 50)
    private String bizOrderNo;

    /** biz_type */
    @Column(name = "biz_type", length = 30)
    private String bizType;

    /** approval_status */
    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    /** process_instance_id */
    @Column(name = "process_instance_id", length = 50)
    private String processInstanceId;

    /** attachment_urls */
    @Column(name = "attachment_urls", columnDefinition = "text")
    private String attachmentUrls;

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

    /** 加油时里程 */
    @Column(name = "current_mileage")
    private BigDecimal currentMileage;

    /** 加油量（升） */
    @Column(name = "fuel_quantity")
    private BigDecimal fuelQuantity;

    /** 审批人ID */
    @Column(name = "approval_user_id")
    private Long approvalUserId;

    /** 审批时间 */
    @Column(name = "approval_time")
    private OffsetDateTime approvalTime;

    /** 审批意见 */
    @Column(name = "approval_remark", columnDefinition = "text")
    private String approvalRemark;

}
