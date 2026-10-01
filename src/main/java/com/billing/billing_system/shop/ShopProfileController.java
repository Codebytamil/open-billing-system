package com.billing.billing_system.shop;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop")
public class ShopProfileController {

    private final ShopProfileService service;

    public ShopProfileController(ShopProfileService service) {
        this.service = service;
    }

    @PutMapping
    public ShopProfile save(@Valid @RequestBody ShopProfile profile) {
        return service.save(profile);
    }

    @GetMapping
    public ShopProfile get() {
        return service.get();
    }
}