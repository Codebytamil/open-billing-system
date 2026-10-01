package com.billing.billing_system.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductIdOrderByIdDesc(Long productId);
}