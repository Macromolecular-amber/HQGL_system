package com.logistics.dto.st;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 消费统计响应
 */
@Data
public class ConsumeStatisticsVO {

    /** 消费总金额 */
    private BigDecimal totalAmount;

    /** 消费笔数 */
    private Integer totalCount;

    /** 人均消费金额 */
    private BigDecimal avgAmountPerPerson;

    /** 预约总人数 */
    private Integer totalReservations;

    /** 每日消费趋势 */
    private List<DailyConsumeItem> dailyTrend;
}
