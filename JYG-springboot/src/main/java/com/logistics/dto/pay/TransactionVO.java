package com.logistics.dto.pay;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 交易流水视图：包含 PayTransaction 全部字段，并补充中文名与用户姓名
 */
@Data
public class TransactionVO {

    /** id */
    private Long id;

    /** 交易流水号 */
    private String transactionNo;

    /** 外部交易号 */
    private String externalTransactionNo;

    /** 用户ID */
    private Long userId;

    /** 账户类型 */
    private String accountType;

    /** 账户号 */
    private String accountNo;

    /** 卡号 */
    private String cardNo;

    /** 交易类型：RECHARGE / CONSUME / REFUND */
    private String transactionType;

    /** 交易金额（充值/退款为正，消费为负） */
    private BigDecimal amount;

    /** 交易前余额 */
    private BigDecimal balanceBefore;

    /** 交易后余额 */
    private BigDecimal balanceAfter;

    /** 支付方式 */
    private String payMethod;

    /** 支付状态 */
    private String payStatus;

    /** 业务模块 */
    private String bizModule;

    /** 业务单号 */
    private String bizOrderNo;

    /** 场景 */
    private String scene;

    /** 备注 */
    private String remark;

    /** 支付时间 */
    private OffsetDateTime payTime;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 交易类型中文名 */
    private String transactionTypeLabel;

    /** 支付方式中文名 */
    private String payMethodLabel;

    /** 用户姓名 */
    private String userName;
}
