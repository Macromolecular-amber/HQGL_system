package com.logistics.common;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = Result.ERROR_CODE;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
