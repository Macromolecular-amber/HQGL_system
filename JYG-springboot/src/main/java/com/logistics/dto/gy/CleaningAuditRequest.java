package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 公寓保洁审批请求
 */
@Data
public class CleaningAuditRequest {

    /** 保洁单ID（必填） */
    @NotNull(message = "保洁单ID不能为空")
    private Long cleaningId;

    /** 审批结果：PASS 或 REJECT（必填） */
    @NotBlank(message = "审批结果不能为空")
    private String auditResult;

    /** 审批意见 */
    private String auditRemark;
}
