package com.logistics.common;

import lombok.Data;

/**
 * 统一响应结果
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {

    /** 状态码，200 表示成功 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    public static final Integer SUCCESS_CODE = 200;
    public static final Integer ERROR_CODE = 500;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
