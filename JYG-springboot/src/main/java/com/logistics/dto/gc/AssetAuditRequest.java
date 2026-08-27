package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 资产入仓审核请求
 */
@Data
public class AssetAuditRequest {

    /** 资产ID */
    @NotNull(message = "资产ID不能为空")
    private Long id;

    /** 审核结果：PASS 通过 / REJECT 驳回 */
    @NotBlank(message = "审核结果不能为空")
    private String auditResult;

    /** 审核意见 */
    private String auditRemark;
}
