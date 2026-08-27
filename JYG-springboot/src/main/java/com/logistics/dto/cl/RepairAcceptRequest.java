package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 维修验收请求
 */
@Data
public class RepairAcceptRequest {

    /** 维修单ID（必填） */
    @NotNull(message = "维修单ID不能为空")
    private Long repairId;

    /** 验收结果：PASS 或 FAIL（必填） */
    @NotBlank(message = "验收结果不能为空")
    private String acceptResult;

    /** 实际费用（必填） */
    @NotNull(message = "实际费用不能为空")
    private BigDecimal actualCost;

    /** 配件明细 */
    private List<PartsItemDTO> partsDetail;

    /** 工时费 */
    private BigDecimal laborCost;

    /** 验收意见 */
    private String acceptRemark;
}
