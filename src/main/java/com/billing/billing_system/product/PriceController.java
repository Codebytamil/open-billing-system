package com.billing.billing_system.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products/{id}")
public class PriceController {

    public record PriceUpdateRequest(
            @NotNull(message = "newPrice is required")
            @DecimalMin(value = "0.01", message = "newPrice must be greater than 0")
            @Digits(integer = 8, fraction = 2,
                    message = "newPrice can have at most 2 decimal places")
            BigDecimal newPrice
    ) {
    }

    private final PriceService service;

    public PriceController(PriceService service) {
        this.service = service;
    }

    @PutMapping("/price")
    public Product updatePrice(@PathVariable Long id,
                               @Valid @RequestBody PriceUpdateRequest request,
                               Authentication authentication) {
        return service.updatePrice(id, request.newPrice(), authentication.getName());
    }

    @GetMapping("/price-history")
    public List<PriceHistory> history(@PathVariable Long id) {
        return service.getHistory(id);
    }
}