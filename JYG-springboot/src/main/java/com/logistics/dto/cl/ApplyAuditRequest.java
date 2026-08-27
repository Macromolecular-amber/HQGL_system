package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用车申请审批请求
 */
@Data
public class ApplyAuditRequest {

    /** 申请ID */
    @NotNull(message = "申请ID不能为空")
    private Long applyId;

    /** 审批结果：PASS 通过 / REJECT 驳回 */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
