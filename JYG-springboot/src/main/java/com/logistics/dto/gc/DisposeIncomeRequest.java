package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 处置收益录入请求
 */
@Data
public class DisposeIncomeRequest {

    /** 处置单ID */
    @NotNull(message = "处置单ID不能为空")
    private Long orderId;

    /** 处置收入 */
    @NotNull(message = "处置收入不能为空")
    @DecimalMin(value = "0", message = "处置收入不能小于0")
    private BigDecimal incomeAmount;

    /** 处置费用 */
    @NotNull(message = "处置费用不能为空")
    @DecimalMin(value = "0", message = "处置费用不能小于0")
    private BigDecimal expenseAmount;

    /** 备注 */
    private String remark;
}
