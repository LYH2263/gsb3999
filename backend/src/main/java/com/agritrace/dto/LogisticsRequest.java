package com.agritrace.dto;
import lombok.Data;
@Data
public class LogisticsRequest {
    private String traceCode;
    private String location;
    private String statusDesc;
}
