package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 用车申请视图：包含 ClApplyOrder 全部字段，并补充申请人/单位名称、车型与状态中文名、可调度车辆
 */
@Data
public class ApplyVO {

    /** id */
    private Long id;

    /** 申请编号 */
    private String applyNo;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请单位ID */
    private Long applicantUnitId;

    /** 申请人电话 */
    private String applicantPhone;

    /** 用车事由 */
    private String purpose;

    /** 目的地 */
    private String destination;

    /** 备注 */
    private String remark;

    /** 开始时间 */
    private OffsetDateTime startTime;

    /** 结束时间 */
    private OffsetDateTime endTime;

    /** 乘车人数 */
    private Integer passengerCount;

    /** 所需车型 */
    private String requiredVehicleType;

    /** 规划路线 */
    private String plannedRoute;

    /** 规划里程 */
    private BigDecimal plannedMileage;

    /** 申请状态 */
    private String applyStatus;

    /** 是否自动审批 */
    private Boolean autoApprove;

    /** 驳回原因 */
    private String rejectReason;

    /** 调度单ID */
    private Long dispatchOrderId;

    /** 流程实例ID */
    private String processInstanceId;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 逻辑删除 */
    private Boolean isDeleted;

    /** 审批人ID */
    private Long auditUserId;

    /** 审批人姓名 */
    private String auditUserName;

    /** 审批时间 */
    private OffsetDateTime auditTime;

    /** 审批意见 */
    private String auditRemark;

    /** 申请单位名称 */
    private String applicantUnitName;

    /** 车型中文名 */
    private String vehicleTypeLabel;

    /** 状态中文名 */
    private String statusLabel;

    /** 可调度车辆（供前端展示） */
    private List<VehicleSimpleVO> availableVehicles;
}
