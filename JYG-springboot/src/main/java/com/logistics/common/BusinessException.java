package com.logistics.common;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    /** 业务/参数校验错误码（400 客户端错误，区别于 500 服务器错误） */
    private static final int BIZ_ERROR_CODE = 400;

    public BusinessException(String message) {
        super(message);
        this.code = BIZ_ERROR_CODE;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
