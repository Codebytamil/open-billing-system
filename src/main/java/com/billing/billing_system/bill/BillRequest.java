package com.billing.billing_system.bill;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record BillRequest(

        @NotBlank(message = "paymentMode is required")
        @Pattern(regexp = "CASH|UPI|CARD", message = "paymentMode must be CASH, UPI or CARD")
        String paymentMode,

        @Size(max = 100, message = "customerName is too long")
        String customerName,

        @Pattern(regexp = "[0-9]{10}", message = "customerPhone must be exactly 10 digits")
        String customerPhone,

        @Size(max = 15, message = "customerGstin must be at most 15 characters")
        String customerGstin,

        @NotEmpty(message = "Bill must have at least one item")
        @Valid
        List<ItemRequest> items
) {
    public record ItemRequest(
            @NotNull(message = "productId is required")
            Long productId,

            @Min(value = 1, message = "quantity must be at least 1")
            int quantity
    ) {
    }
}