package com.buaa.clouddisk.common.result;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源未找到"),
    ERROR(500, "系统内部错误");

    public static ResultCode FAIL;
    public int code;
    public String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}