package com.logistics.dto.gy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 公寓维修单视图：包含 GyRepairOrder 全部字段，并补充房间信息与中文名
 */
@Data
public class RepairVO {

    /** id */
    private Long id;

    /** 维修单编号 */
    private String repairNo;

    /** 房间ID */
    private Long roomId;

    /** 房间号 */
    private String roomNo;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请人电话 */
    private String applicantPhone;

    /** 故障位置 */
    private String faultLocation;

    /** 故障描述 */
    private String faultDesc;

    /** 故障照片 */
    private String faultPhotos;

    /** 紧急程度 */
    private String urgencyLevel;

    /** 费用承担方式 */
    private String costType;

    /** 预估费用 */
    private BigDecimal estimatedCost;

    /** 实际费用 */
    private BigDecimal actualCost;

    /** 报价明细 */
    private String quoteDetail;

    /** 单据状态 */
    private String orderStatus;

    /** 流程实例ID */
    private String processInstanceId;

    /** 维修厂ID */
    private Long repairShopId;

    /** 维修厂名称 */
    private String repairShopName;

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

    /** 验收照片 */
    private String acceptPhotos;

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

    /** 维修类型 */
    private String repairType;

    /** 配件明细 */
    private List<PartsItemDTO> partsDetail;

    /** 工时费 */
    private BigDecimal laborCost;

    /** 审批人ID */
    private Long auditUserId;

    /** 审批人姓名 */
    private String auditUserName;

    /** 审批时间 */
    private OffsetDateTime auditTime;

    /** 审批意见 */
    private String auditRemark;

    /** 楼栋 */
    private String building;

    /** 公寓类型 */
    private String roomType;

    /** 维修类型中文名 */
    private String repairTypeLabel;

    /** 费用承担中文名 */
    private String costTypeLabel;

    /** 状态中文名 */
    private String statusLabel;
}
