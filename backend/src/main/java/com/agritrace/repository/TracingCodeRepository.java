package com.agritrace.repository;
import com.agritrace.entity.TracingCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TracingCodeRepository extends JpaRepository<TracingCode, Long> {
    Optional<TracingCode> findByTraceCode(String traceCode);
    List<TracingCode> findByProductId(Long productId);
}
