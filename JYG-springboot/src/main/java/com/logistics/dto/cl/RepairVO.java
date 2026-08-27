package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 维修单视图：包含 ClRepairOrder 全部字段，并补充车辆信息与中文名、配件明细
 */
@Data
public class RepairVO {

    /** id */
    private Long id;

    /** 维修单编号 */
    private String repairNo;

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNumber;

    /** 维修类型 */
    private String repairType;

    /** 故障描述 */
    private String faultDesc;

    /** 故障照片 */
    private String faultPhotos;

    /** 紧急程度 */
    private String urgencyLevel;

    /** 维修厂ID */
    private Long repairShopId;

    /** 维修厂名称 */
    private String repairShopName;

    /** 预估费用 */
    private BigDecimal estimatedCost;

    /** 实际费用 */
    private BigDecimal actualCost;

    /** 配件明细 */
    private List<PartsItemDTO> partsDetail;

    /** 工时费 */
    private BigDecimal laborCost;
    /** 单据状态 */
    private String orderStatus;

    /** 流程实例ID */
    private String processInstanceId;

    /** 开始维修时间 */
    private OffsetDateTime repairStart;

    /** 维修结束时间 */
    private OffsetDateTime repairEnd;

    /** 维修照片 */
    private String repairPhotos;

    /** 验收人ID */
    private Long acceptUserId;

    /** 验收时间 */
    private OffsetDateTime acceptTime;

    /** 验收结果 */
    private String acceptResult;

    /** 验收意见 */
    private String acceptRemark;

    /** 维修里程 */
    private BigDecimal repairMileage;

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

    /** 车型中文名 */
    private String vehicleTypeLabel;

    /** 维修类型中文名 */
    private String repairTypeLabel;

    /** 状态中文名 */
    private String statusLabel;
}
