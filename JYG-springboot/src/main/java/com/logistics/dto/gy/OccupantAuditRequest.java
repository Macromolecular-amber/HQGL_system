package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 人才公寓入住审批请求
 */
@Data
public class OccupantAuditRequest {

    /** 入住/申请记录ID（必填） */
    @NotNull(message = "记录ID不能为空")
    private Long occupantId;

    /** 审批结果：PASS 或 REJECT（必填） */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;

    /** 审批通过后分配的房间ID（人才公寓选填） */
    private Long roomId;
}
