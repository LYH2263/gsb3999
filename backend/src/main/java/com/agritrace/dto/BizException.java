package com.agritrace.dto;

/**
 * 业务异常，由 Service 层抛出，携带 HTTP 语义状态码。
 * 由 GlobalExceptionHandler 统一转换为 Result，使 Controller 保持纯净。
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
