package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 采购单审批请求
 */
@Data
public class PurchaseAuditRequest {

    /** 采购单ID（必填） */
    @NotNull(message = "采购单ID不能为空")
    private Long orderId;

    /** 审批结果（必填）：PASS 通过 / REJECT 驳回 */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
