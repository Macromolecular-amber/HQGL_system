package com.logistics.dto.gy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 公寓保洁单视图：包含 GyCleaningOrder 全部字段，并补充房间信息与中文名
 */
@Data
public class CleaningVO {

    /** id */
    private Long id;

    /** 保洁单编号 */
    private String cleaningNo;

    /** 房间ID */
    private Long roomId;

    /** 房间号 */
    private String roomNo;

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 保洁时间 */
    private OffsetDateTime cleaningTime;

    /** 保洁范围 */
    private String cleaningScope;

    /** 保洁要求 */
    private String cleaningRequirement;

    /** 单据状态 */
    private String orderStatus;

    /** 保洁员ID */
    private Long assigneeId;

    /** 保洁员姓名 */
    private String assigneeName;

    /** 保洁公司 */
    private String assigneeCompany;

    /** 派单时间 */
    private OffsetDateTime assignTime;

    /** 执行时间 */
    private OffsetDateTime executeTime;

    /** 执行照片 */
    private String executePhotos;

    /** 验收人ID */
    private Long acceptUserId;

    /** 验收时间 */
    private OffsetDateTime acceptTime;

    /** 验收结果 */
    private String acceptResult;

    /** 验收意见 */
    private String acceptRemark;

    /** 验收评分 */
    private Integer acceptScore;

    /** 结算金额 */
    private BigDecimal settleAmount;

    /** 结算状态 */
    private String settleStatus;

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

    /** 保洁类型 */
    private String cleaningType;

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

    /** 保洁类型中文名 */
    private String cleaningTypeLabel;

    /** 状态中文名 */
    private String statusLabel;
}
