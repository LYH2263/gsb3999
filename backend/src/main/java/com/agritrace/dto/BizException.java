package com.agritrace.dto;
import lombok.Getter;

/** 业务异常：由 Service 抛出，统一由 GlobalExceptionHandler 转为 Result */
@Getter
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
