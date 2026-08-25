package com.agritrace.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "farming_record")
public class FarmingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType;

    @Column(name = "operation_date", nullable = false)
    private LocalDate operationDate;

    @Column(name = "pesticide_name", length = 100)
    private String pesticideName;

    @Column(name = "safety_interval_days")
    private Integer safetyIntervalDays;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
