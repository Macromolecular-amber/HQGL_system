package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * pay_transaction 实体
 */
@Data
@Entity
@Table(name = "pay_transaction")
public class PayTransaction {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** transaction_no */
    @Column(name = "transaction_no", length = 50)
    private String transactionNo;

    /** external_transaction_no */
    @Column(name = "external_transaction_no", length = 50)
    private String externalTransactionNo;

    /** user_id */
    @Column(name = "user_id")
    private Long userId;

    /** account_type */
    @Column(name = "account_type", length = 20)
    private String accountType;

    /** account_no */
    @Column(name = "account_no", length = 50)
    private String accountNo;

    /** card_no */
    @Column(name = "card_no", length = 50)
    private String cardNo;

    /** transaction_type */
    @Column(name = "transaction_type", length = 20)
    private String transactionType;

    /** amount */
    @Column(name = "amount")
    private BigDecimal amount;

    /** balance_before */
    @Column(name = "balance_before")
    private BigDecimal balanceBefore;

    /** balance_after */
    @Column(name = "balance_after")
    private BigDecimal balanceAfter;

    /** pay_method */
    @Column(name = "pay_method", length = 20)
    private String payMethod;

    /** pay_status */
    @Column(name = "pay_status", length = 20)
    private String payStatus;

    /** biz_module */
    @Column(name = "biz_module", length = 30)
    private String bizModule;

    /** biz_order_no */
    @Column(name = "biz_order_no", length = 50)
    private String bizOrderNo;

    /** scene */
    @Column(name = "scene", length = 50)
    private String scene;

    /** remark */
    @Column(name = "remark", length = 255)
    private String remark;

    /** pay_time */
    @Column(name = "pay_time")
    private OffsetDateTime payTime;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
