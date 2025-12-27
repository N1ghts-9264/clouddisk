package com.buaa.clouddisk.module.file.exception;

import com.buaa.clouddisk.common.result.Result;
import com.buaa.clouddisk.common.result.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @deprecated 与 com.buaa.clouddisk.common.exception.BusinessException 重复
 * 建议后续统一使用 common 包下的异常类
 * 保留此类避免编译错误
 */
@Deprecated
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessException extends RuntimeException {
    private final ResultCode code;

    public BusinessException(String message) {
        this(ResultCode.FAIL, message);
    }

    public BusinessException(ResultCode code, String message) {
        super(message);
        this.code = code;
    }

    public ResultCode getCode() {
        return code;
    }
}