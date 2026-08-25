package com.agritrace.repository;
import com.agritrace.entity.HotProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface HotProductRepository extends JpaRepository<HotProduct, Long> {
    List<HotProduct> findByIsDisplayOrderBySearchCountDesc(Integer isDisplay);
    Optional<HotProduct> findByProductId(Long productId);
}
