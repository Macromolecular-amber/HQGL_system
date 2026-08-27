package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 公寓维修审批请求
 */
@Data
public class RepairAuditRequest {

    /** 维修单ID（必填） */
    @NotNull(message = "维修单ID不能为空")
    private Long repairId;

    /** 审批结果：PASS 或 REJECT（必填） */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
