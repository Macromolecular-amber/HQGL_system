package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 处置审批请求
 */
@Data
public class DisposeAuditRequest {

    /** 处置单ID */
    @NotNull(message = "处置单ID不能为空")
    private Long orderId;

    /** 审批结果：PASS 通过 / REJECT 驳回 */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
