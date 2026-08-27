package com.logistics.dto.gc;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 归还单视图：包含 GcReturnOrder 全部字段，并补充借用单号、申请人信息与资产明细
 */
@Data
public class ReturnOrderVO {

    /** id */
    private Long id;

    /** 归还单编号 */
    private String returnNo;

    /** 借用单ID */
    private Long borrowOrderId;

    /** 归还申请人ID */
    private Long returnApplicantId;

    /** 归还申请单位ID */
    private Long returnApplicantUnitId;

    /** 计划归还时间 */
    private OffsetDateTime planReturnTime;

    /** 实际归还时间 */
    private OffsetDateTime actualReturnTime;

    /** 验收人ID（逗号分隔） */
    private String acceptUserIds;

    /** 验收时间 */
    private OffsetDateTime acceptTime;

    /** 验收结果 */
    private String acceptResult;

    /** 验收意见 */
    private String acceptRemark;

    /** 验收照片 */
    private String acceptPhotos;

    /** 损坏描述 */
    private String damageInfo;

    /** 损坏责任 */
    private String damageResponsibility;

    /** 维修费用 */
    private BigDecimal repairCost;

    /** 赔偿金额 */
    private BigDecimal compensationAmount;

    /** 归还单状态 */
    private String returnStatus;

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

    /** 借用单编号 */
    private String borrowOrderNo;

    /** 申请人姓名 */
    private String applicantName;

    /** 申请单位名称 */
    private String applicantUnitName;

    /** 归还资产数量 */
    private Integer assetCount;

    /** 归还资产明细 */
    private List<ReturnDetailVO> detailList;
}
