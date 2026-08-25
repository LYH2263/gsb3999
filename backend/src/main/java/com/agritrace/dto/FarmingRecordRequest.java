package com.agritrace.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FarmingRecordRequest {
    private Long productId;
    private String operationType;
    private LocalDate operationDate;
    private String pesticideName;
    private Integer safetyIntervalDays;
    private String remark;
}
