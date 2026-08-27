package com.logistics.dto.pay;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 餐卡充值请求
 */
@Data
public class RechargeRequest {

    /** 用户ID（必填） */
    @NotNull(message = "用户不能为空")
    private Long userId;

    /** 充值金额（必填，>0） */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    /** 备注 */
    private String remark;
}
