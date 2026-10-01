package com.billing.billing_system.search;

import com.billing.billing_system.bill.Bill;
import com.billing.billing_system.product.Product;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping("/products")
    public PageResponse<Product> products(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchProducts(q, minPrice, maxPrice, inStock, page, size);
    }

    @GetMapping("/bills")
    public PageResponse<Bill> bills(
            @RequestParam(required = false) String billNumber,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String paymentMode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchBills(billNumber, customer, phone, paymentMode,
                from, to, minTotal, maxTotal, page, size);
    }

    @GetMapping("/transactions")
    public PageResponse<TransactionResponse> transactions(
            @RequestParam(required = false) String product,
            @RequestParam(required = false) String billNumber,
            @RequestParam(required = false) String paymentMode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchTransactions(product, billNumber, paymentMode, from, to, page, size);
    }
}