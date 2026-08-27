package com.logistics.dto.pay;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 餐卡消费扣款请求
 */
@Data
public class ConsumeRequest {

    /** 用户ID（必填） */
    @NotNull(message = "用户不能为空")
    private Long userId;

    /** 消费金额（必填，>0） */
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    /** 业务单号（如预约订单号） */
    private String bizOrderNo;

    /** 备注 */
    private String remark;
}
