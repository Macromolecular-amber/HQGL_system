package com.logistics.dto.gc;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 处置单视图：包含 GcTransferOrder 全部字段，并补充单位名称、资产明细与处置方式中文名
 */
@Data
public class DisposeOrderVO {

    /** id */
    private Long id;

    /** 处置单编号 */
    private String orderNo;

    /** 调剂类型：TRANSFER 调剂 / DISPOSE 处置 */
    private String transferType;

    /** 资产数量 */
    private Integer assetCount;

    /** 资产总值 */
    private BigDecimal totalValue;

    /** 申请单位ID */
    private Long applicantUnitId;

    /** 接收单位ID */
    private Long receiveUnitId;

    /** 调剂事由 */
    private String applyReason;

    /** 备注 */
    private String remark;

    /** 处置方式 */
    private String disposeMethod;

    /** 评估机构 */
    private String appraisalOrg;

    /** 评估价值 */
    private BigDecimal appraisalValue;

    /** 评估报告URL */
    private String appraisalReportUrl;

    /** 评估时间 */
    private OffsetDateTime appraisalTime;

    /** 调剂单状态 */
    private String orderStatus;

    /** 执行时间 */
    private OffsetDateTime execTime;

    /** 执行结果 */
    private String execResult;

    /** 收入金额 */
    private BigDecimal incomeAmount;

    /** 支出金额 */
    private BigDecimal expenseAmount;

    /** 净收益（数据库生成列） */
    private BigDecimal netProfit;

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

    /** 处置方式中文名（AUCTION 拍卖 / SCRAP 报废 / DONATE 捐赠） */
    private String disposeMethodLabel;

    /** 处置资产明细 */
    private List<TransferDetailVO> detailList;
}
