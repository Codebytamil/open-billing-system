package com.billing.billing_system.bill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BillItemRepository extends JpaRepository<BillItem, Long>, JpaSpecificationExecutor<BillItem> {
}