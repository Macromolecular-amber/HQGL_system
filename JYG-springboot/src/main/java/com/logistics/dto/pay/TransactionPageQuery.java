package com.logistics.dto.pay;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 交易流水分页查询条件
 */
@Data
public class TransactionPageQuery {

    /** 用户ID */
    private Long userId;

    /** 交易类型：RECHARGE 充值 / CONSUME 消费 / REFUND 退款 */
    private String transactionType;

    /** 起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
