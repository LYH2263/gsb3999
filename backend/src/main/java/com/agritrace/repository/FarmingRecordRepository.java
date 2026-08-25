package com.agritrace.repository;

import com.agritrace.entity.FarmingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmingRecordRepository extends JpaRepository<FarmingRecord, Long> {

    List<FarmingRecord> findByProductIdOrderByOperationDateDesc(Long productId);

    List<FarmingRecord> findByFarmerIdOrderByOperationDateDesc(Long farmerId);

    List<FarmingRecord> findByProductIdAndOperationType(Long productId, String operationType);
}
