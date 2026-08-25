package com.agritrace.repository;
import com.agritrace.entity.FarmingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FarmingRecordRepository extends JpaRepository<FarmingRecord, Long> {
    List<FarmingRecord> findByFarmerIdOrderByOperationDateDesc(Long farmerId);
    List<FarmingRecord> findByProductIdOrderByOperationDateAsc(Long productId);
    List<FarmingRecord> findByProductIdAndOperationType(Long productId, String operationType);
}
