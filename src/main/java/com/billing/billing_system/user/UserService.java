package com.billing.billing_system.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(AppUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public AppUser createCashier(String username, String password) {
        if (repo.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(password));
        user.setRole("CASHIER");
        return repo.save(user);
    }
}