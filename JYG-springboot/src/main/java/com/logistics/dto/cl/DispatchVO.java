package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 调度单视图：包含 ClDispatchOrder 全部字段，并补充申请信息与车辆/驾驶员中文名
 */
@Data
public class DispatchVO {

    /** id */
    private Long id;

    /** 派单编号 */
    private String dispatchNo;

    /** 关联申请ID */
    private Long applyId;

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNumber;

    /** 驾驶员ID */
    private Long driverId;

    /** 驾驶员姓名 */
    private String driverName;

    /** 驾驶员电话 */
    private String driverPhone;

    /** 计划开始时间 */
    private OffsetDateTime scheduledStart;

    /** 计划结束时间 */
    private OffsetDateTime scheduledEnd;

    /** 实际开始时间 */
    private OffsetDateTime actualStart;

    /** 实际结束时间 */
    private OffsetDateTime actualEnd;

    /** 实际行驶里程 */
    private BigDecimal actualMileage;

    /** 调度状态 */
    private String dispatchStatus;

    /** 是否紧急 */
    private Boolean isEmergency;

    /** 紧急原因 */
    private String emergencyReason;

    /** 备注 */
    private String remark;

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

    /** 关联申请编号 */
    private String applyNo;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请单位名称 */
    private String applicantUnitName;

    /** 用车事由 */
    private String purpose;

    /** 目的地 */
    private String destination;

    /** 车型中文名 */
    private String vehicleTypeLabel;

    /** 状态中文名 */
    private String statusLabel;
}
