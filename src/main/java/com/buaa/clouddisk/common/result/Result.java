package com.buaa.clouddisk.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.msg, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.ERROR.code, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}