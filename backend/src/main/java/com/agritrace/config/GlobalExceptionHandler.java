package com.agritrace.config;
import com.agritrace.dto.BizException;
import com.agritrace.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 将 Service 层抛出的业务异常统一转换为 Result 响应 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
