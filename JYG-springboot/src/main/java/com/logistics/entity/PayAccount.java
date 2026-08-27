package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * pay_account 实体
 */
@Data
@Entity
@Table(name = "pay_account")
public class PayAccount {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** user_id */
    @Column(name = "user_id")
    private Long userId;

    /** account_type */
    @Column(name = "account_type", length = 20)
    private String accountType;

    /** account_no */
    @Column(name = "account_no", length = 50)
    private String accountNo;

    /** balance */
    @Column(name = "balance")
    private BigDecimal balance;

    /** frozen_amount */
    @Column(name = "frozen_amount")
    private BigDecimal frozenAmount;

    /** card_no */
    @Column(name = "card_no", length = 50)
    private String cardNo;

    /** card_status */
    @Column(name = "card_status", length = 20)
    private String cardStatus;

    /** account_status */
    @Column(name = "account_status", length = 20)
    private String accountStatus;

    /** last_transaction_time */
    @Column(name = "last_transaction_time")
    private OffsetDateTime lastTransactionTime;

    /** create_by */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_by */
    @Column(name = "update_by")
    private Long updateBy;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    /** is_deleted */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
