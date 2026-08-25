package com.agritrace.repository;
import com.agritrace.entity.FarmingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FarmingRecordRepository extends JpaRepository<FarmingRecord, Long> {
    List<FarmingRecord> findByProductIdOrderByOperationDateDescIdDesc(Long productId);
    List<FarmingRecord> findByFarmerIdOrderByOperationDateDescIdDesc(Long farmerId);
    List<FarmingRecord> findByProductIdAndOperationTypeOrderByOperationDateDescIdDesc(Long productId, String operationType);
}
