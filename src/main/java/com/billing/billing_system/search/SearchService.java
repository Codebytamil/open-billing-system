package com.billing.billing_system.search;

import com.billing.billing_system.bill.Bill;
import com.billing.billing_system.bill.BillItem;
import com.billing.billing_system.bill.BillItemRepository;
import com.billing.billing_system.bill.BillRepository;
import com.billing.billing_system.product.Product;
import com.billing.billing_system.product.ProductRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    private final ProductRepository productRepo;
    private final BillRepository billRepo;
    private final BillItemRepository itemRepo;

    public SearchService(ProductRepository productRepo,
                         BillRepository billRepo,
                         BillItemRepository itemRepo) {
        this.productRepo = productRepo;
        this.billRepo = billRepo;
        this.itemRepo = itemRepo;
    }

    // ---------- helpers ----------

    private Pageable pageable(int page, int size, Sort sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, sort);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String like(String s) {
        return "%" + s.trim().toLowerCase() + "%";
    }

    // ---------- products ----------

    public PageResponse<Product> searchProducts(String q, BigDecimal minPrice,
                                                BigDecimal maxPrice, Boolean inStock,
                                                int page, int size) {

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();

            if (hasText(q)) {
                filters.add(cb.or(
                        cb.like(cb.lower(root.<String>get("name")), like(q)),
                        cb.like(cb.lower(root.<String>get("sku")), like(q))));
            }
            if (minPrice != null) {
                filters.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice));
            }
            if (maxPrice != null) {
                filters.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice));
            }
            if (inStock != null) {
                if (inStock) {
                    filters.add(cb.greaterThan(root.<Integer>get("stock"), 0));
                } else {
                    filters.add(cb.equal(root.<Integer>get("stock"), 0));
                }
            }
            return cb.and(filters.toArray(new Predicate[0]));
        };

        Page<Product> result = productRepo.findAll(spec, pageable(page, size, Sort.by("name", "id")));
        return PageResponse.of(result);
    }

    // ---------- bills ----------

    public PageResponse<Bill> searchBills(String billNumber, String customer, String phone,
                                          String paymentMode, LocalDate from, LocalDate to,
                                          BigDecimal minTotal, BigDecimal maxTotal,
                                          int page, int size) {

        Specification<Bill> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();

            if (hasText(billNumber)) {
                filters.add(cb.like(cb.lower(root.<String>get("billNumber")), like(billNumber)));
            }
            if (hasText(customer)) {
                filters.add(cb.like(cb.lower(root.<String>get("customerName")), like(customer)));
            }
            if (hasText(phone)) {
                filters.add(cb.like(root.<String>get("customerPhone"), "%" + phone.trim() + "%"));
            }
            if (hasText(paymentMode)) {
                filters.add(cb.equal(root.<String>get("paymentMode"), paymentMode.trim().toUpperCase()));
            }
            if (from != null) {
                filters.add(cb.greaterThanOrEqualTo(
                        root.<LocalDateTime>get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                filters.add(cb.lessThan(
                        root.<LocalDateTime>get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            if (minTotal != null) {
                filters.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("totalAmount"), minTotal));
            }
            if (maxTotal != null) {
                filters.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("totalAmount"), maxTotal));
            }
            return cb.and(filters.toArray(new Predicate[0]));
        };

        Page<Bill> result = billRepo.findAll(spec,
                pageable(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.of(result);
    }

    // ---------- transactions (sold items) ----------

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> searchTransactions(String product, String billNumber,
                                                                String paymentMode,
                                                                LocalDate from, LocalDate to,
                                                                int page, int size) {

        Specification<BillItem> spec = (root, query, cb) -> {
            Join<BillItem, Bill> bill = root.join("bill");
            List<Predicate> filters = new ArrayList<>();

            if (hasText(product)) {
                filters.add(cb.like(cb.lower(root.<String>get("productName")), like(product)));
            }
            if (hasText(billNumber)) {
                filters.add(cb.like(cb.lower(bill.<String>get("billNumber")), like(billNumber)));
            }
            if (hasText(paymentMode)) {
                filters.add(cb.equal(bill.<String>get("paymentMode"), paymentMode.trim().toUpperCase()));
            }
            if (from != null) {
                filters.add(cb.greaterThanOrEqualTo(
                        bill.<LocalDateTime>get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                filters.add(cb.lessThan(
                        bill.<LocalDateTime>get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            return cb.and(filters.toArray(new Predicate[0]));
        };

        Page<BillItem> result = itemRepo.findAll(spec,
                pageable(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.of(result.map(this::toTransaction));
    }

    private TransactionResponse toTransaction(BillItem item) {
        Bill bill = item.getBill();
        return new TransactionResponse(
                bill.getId(),
                bill.getBillNumber(),
                bill.getCreatedAt(),
                bill.getPaymentMode(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getGstAmount(),
                item.getLineTotal());
    }
}