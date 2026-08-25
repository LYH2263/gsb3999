package com.agritrace.repository;
import com.agritrace.entity.Logistics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LogisticsRepository extends JpaRepository<Logistics, Long> {
    List<Logistics> findByTraceCodeId(Long traceCodeId);
    boolean existsByLogisticsAdminId(Long logisticsAdminId);
}
