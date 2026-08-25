package com.agritrace.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logistics")
public class Logistics {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long traceCodeId;
    private Long logisticsAdminId;
    private String location;
    private String statusDesc;
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}
