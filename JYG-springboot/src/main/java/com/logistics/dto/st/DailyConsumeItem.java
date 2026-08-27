package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日消费统计项
 */
@Data
public class DailyConsumeItem {

    /** 日期 */
    private LocalDate date;

    /** 消费金额 */
    private BigDecimal totalAmount;

    /** 消费笔数 */
    private Integer count;
}
