package com.billing.billing_system.security;

import com.billing.billing_system.user.AppUser;
import com.billing.billing_system.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AppUserRepository repo;
    private final PasswordEncoder encoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminInitializer(AppUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }

        boolean generated = adminPassword == null || adminPassword.isBlank();
        String password = generated ? randomPassword() : adminPassword;

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(encoder.encode(password));
        admin.setRole("ADMIN");
        repo.save(admin);

        System.out.println("Admin user created: " + adminUsername);
        if (generated) {
            System.out.println("Generated admin password (shown only once, save it now): " + password);
        }
    }

    private String randomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}