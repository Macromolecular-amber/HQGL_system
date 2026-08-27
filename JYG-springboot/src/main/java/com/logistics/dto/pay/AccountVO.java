package com.logistics.dto.pay;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 餐卡账户响应
 */
@Data
public class AccountVO {

    /** 用户ID */
    private Long userId;

    /** 用户姓名 */
    private String userName;

    /** 账户类型 */
    private String accountType;

    /** 余额 */
    private BigDecimal balance;

    /** 卡号 */
    private String cardNo;

    /** 卡状态 */
    private String cardStatus;
}
