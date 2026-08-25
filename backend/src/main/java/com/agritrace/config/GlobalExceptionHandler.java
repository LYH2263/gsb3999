package com.agritrace.config;

import com.agritrace.dto.BizException;
import com.agritrace.dto.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理：将 Service 抛出的 BizException 转为标准 Result 响应，
 * 使 Controller 无需编写任何业务判断/错误分支。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<?>> handleBiz(BizException e) {
        HttpStatus status = HttpStatus.resolve(e.getCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Result.error(e.getCode(), e.getMessage()));
    }
}
