package com.agritrace.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FarmingRecordRequest {
    private Long productId;
    /** SOWING / FERTILIZING / PESTICIDE */
    private String operationType;
    private LocalDate operationDate;
    private String drugName;
    private Integer safetyIntervalDays;
    private String remark;
}
