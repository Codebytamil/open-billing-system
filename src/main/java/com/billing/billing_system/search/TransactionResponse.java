package com.billing.billing_system.search;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long billId,
        String billNumber,
        LocalDateTime billDate,
        String paymentMode,
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal gstAmount,
        BigDecimal lineTotal
) {
}