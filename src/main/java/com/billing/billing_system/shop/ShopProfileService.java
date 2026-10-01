package com.billing.billing_system.shop;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShopProfileService {

    private final ShopProfileRepository repo;

    public ShopProfileService(ShopProfileRepository repo) {
        this.repo = repo;
    }

    public ShopProfile save(ShopProfile profile) {
        profile.setId(1L);
        return repo.save(profile);
    }

    public ShopProfile get() {
        return repo.findById(1L).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Shop profile not set. Send PUT /api/shop first"));
    }
}