package com.buaa.clouddisk.common.exception;

import com.buaa.clouddisk.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice // 这个注解非常重要，它会拦截所有 Controller 抛出的异常并转为 JSON
public class GlobalExceptionHandler {

    /**
     * 捕获自定义的业务异常或 RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("请求地址: {}, 业务异常: {}", request.getRequestURI(), e.getMessage());
        // 返回我们统一的 Result 对象，code 设为 500 或 400
        return Result.error(e.getMessage()); 
    }

    /**
     * 捕获其他所有未知的异常 (如数据库连接断开等)
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("服务器系统异常", e);
        return Result.error("服务器开小差了，请稍后再试");
    }
}