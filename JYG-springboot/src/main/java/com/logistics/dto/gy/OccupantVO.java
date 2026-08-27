package com.logistics.dto.gy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 入住记录视图：包含 GyOccupant 全部字段，并补充房间与中文名信息
 */
@Data
public class OccupantVO {

    /** id */
    private Long id;

    /** 入住人姓名 */
    private String occupantName;

    /** 入住人类型 */
    private String occupantType;

    /** 身份证号 */
    private String idCard;

    /** 联系电话 */
    private String phone;

    /** 所属单位ID */
    private Long unitId;

    /** 所属单位名称 */
    private String unitName;

    /** 职务 */
    private String position;

    /** 随行人员信息 */
    private String attendantInfo;

    /** 房间ID */
    private Long roomId;

    /** 房间号 */
    private String roomNo;

    /** 入住时间 */
    private OffsetDateTime checkinTime;

    /** 预计退租时间 */
    private OffsetDateTime expectedLeaveTime;

    /** 实际退租时间 */
    private OffsetDateTime actualLeaveTime;

    /** 租金 */
    private BigDecimal rentAmount;

    /** 租金缴纳状态 */
    private String rentPaidStatus;

    /** 分配方式 */
    private String assignMethod;

    /** 审批文件URL */
    private String approvalFileUrl;

    /** 入住状态 */
    private String occupantStatus;

    /** 关联申请ID */
    private Long applyId;

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

    /** 申请原因 */
    private String applyReason;

    /** 审批人ID */
    private Long auditUserId;

    /** 审批人姓名 */
    private String auditUserName;

    /** 审批时间 */
    private OffsetDateTime auditTime;

    /** 审批意见 */
    private String auditRemark;

    /** 备注 */
    private String remark;

    /** 退住时间 */
    private OffsetDateTime checkoutTime;

    /** 房屋状况 */
    private String roomCondition;

    /** 资产核对结果 */
    private String facilityCheckResult;

    /** 结算金额 */
    private BigDecimal settlementAmount;

    /** 结算明细 */
    private String settlementDetail;

    /** 退房照片 */
    private String checkoutPhotos;

    /** 验收人ID */
    private Long acceptUserId;

    /** 验收人姓名 */
    private String acceptUserName;

    /** 验收时间 */
    private OffsetDateTime acceptTime;

    /** 楼栋 */
    private String building;

    /** 公寓类型中文名 */
    private String roomTypeLabel;

    /** 状态中文名 */
    private String statusLabel;

    /** 分配方式中文名 */
    private String assignMethodLabel;
}
