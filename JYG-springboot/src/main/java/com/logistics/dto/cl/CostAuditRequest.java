package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 费用审批请求
 */
@Data
public class CostAuditRequest {

    /** 费用ID（必填） */
    @NotNull(message = "费用ID不能为空")
    private Long costId;

    /** 审批结果：PASS 或 REJECT（必填） */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
