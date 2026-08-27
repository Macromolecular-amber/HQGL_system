package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 费用视图：包含 ClCostDetail 全部字段，并补充车牌号与类型/状态中文名
 */
@Data
public class CostVO {

    /** id */
    private Long id;

    /** 车辆ID */
    private Long vehicleId;

    /** 费用类型 */
    private String costType;

    /** 费用金额 */
    private BigDecimal costAmount;

    /** 费用时间 */
    private OffsetDateTime costTime;

    /** 费用说明 */
    private String costDesc;

    /** 关联业务单号 */
    private String bizOrderNo;

    /** 业务类型 */
    private String bizType;

    /** 审批状态 */
    private String approvalStatus;

    /** 流程实例ID */
    private String processInstanceId;

    /** 附件URL */
    private String attachmentUrls;

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

    /** 加油时里程 */
    private BigDecimal currentMileage;

    /** 加油量（升） */
    private BigDecimal fuelQuantity;

    /** 审批人ID */
    private Long approvalUserId;

    /** 审批时间 */
    private OffsetDateTime approvalTime;

    /** 审批意见 */
    private String approvalRemark;

    /** 车牌号 */
    private String plateNumber;

    /** 车型中文名 */
    private String vehicleTypeLabel;

    /** 费用类型中文名 */
    private String costTypeLabel;

    /** 审批状态中文名 */
    private String approvalStatusLabel;
}
