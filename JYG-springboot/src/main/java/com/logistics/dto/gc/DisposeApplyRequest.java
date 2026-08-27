package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产处置申请请求
 */
@Data
public class DisposeApplyRequest {

    /** 处置资产ID列表 */
    @NotEmpty(message = "请选择处置资产")
    private List<Long> assetIds;

    /** 处置方式：AUCTION 拍卖 / SCRAP 报废 / DONATE 捐赠 */
    @NotBlank(message = "处置方式不能为空")
    private String disposeMethod;

    /** 处置事由 */
    @NotBlank(message = "处置事由不能为空")
    private String applyReason;

    /** 评估机构名称 */
    private String appraisalOrg;

    /** 评估价值 */
    private BigDecimal appraisalValue;

    /** 评估报告附件URL */
    private String appraisalReportUrl;

    /** 备注 */
    private String remark;
}
