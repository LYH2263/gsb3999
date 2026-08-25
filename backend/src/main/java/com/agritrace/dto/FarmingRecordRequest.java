package com.agritrace.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class FarmingRecordRequest {
    private Long productId;
    /** 操作类型: SOWING(播种), FERTILIZING(施肥), PESTICIDE(用药) */
    private String operationType;
    private LocalDate operationDate;
    private String drugName;
    private Integer safetyIntervalDays;
    private String remark;
}
