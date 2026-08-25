package com.agritrace.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "farming_record")
public class FarmingRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long farmerId;
    /** 操作类型: SOWING(播种), FERTILIZING(施肥), PESTICIDE(用药) */
    private String operationType;
    private LocalDate operationDate;
    /** 用药名称，仅用药记录填写 */
    private String drugName;
    /** 安全间隔天数，仅用药记录有意义 */
    private Integer safetyIntervalDays;
    private String remark;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
