package com.logistics.dto.gc;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 资产借用单视图：包含 GcBorrowOrder 全部字段，并补充申请人姓名、单位名称、资产数量与明细
 */
@Data
public class BorrowOrderVO {

    /** id */
    private Long id;

    /** 借用单编号 */
    private String orderNo;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请单位ID */
    private Long applicantUnitId;

    /** 申请单位名称 */
    private String applicantUnitName;

    /** 申请人电话 */
    private String applicantPhone;

    /** 借用开始时间 */
    private OffsetDateTime borrowStart;

    /** 借用结束时间 */
    private OffsetDateTime borrowEnd;

    /** 借用事由 */
    private String borrowReason;

    /** 借用用途 */
    private String borrowPurpose;

    /** 备注 */
    private String remark;

    /** 借用单状态 */
    private String orderStatus;

    /** 审批状态 */
    private String approvalStatus;

    /** 当前审批人ID */
    private Long currentApproverId;

    /** 当前审批节点 */
    private String currentApproverNode;

    /** 出库时间 */
    private OffsetDateTime warehouseOutTime;

    /** 入库时间 */
    private OffsetDateTime warehouseInTime;

    /** 物流单号 */
    private String logisticsOrderNo;

    /** 物流状态 */
    private String logisticsStatus;

    /** 延期次数 */
    private Integer extensionCount;

    /** 最大延期次数 */
    private Integer maxExtension;

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

    /** 借用资产数量 */
    private Integer assetCount;

    /** 借用明细列表 */
    private List<BorrowDetailVO> detailList;
}
