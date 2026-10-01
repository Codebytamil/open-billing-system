package com.billing.billing_system.product;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product create(Product product) {
        if (repo.existsBySku(product.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        return repo.save(product);
    }

    public List<Product> getAll() {
        return repo.findAll();
    }

    public Product getById(Long id) {
        return repo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public ImportResult importCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            reader.readLine(); // skip the header line
            int lineNo = 1;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    errors.add("Line " + lineNo + ": expected 4 columns (sku,name,price,stock)");
                    continue;
                }

                try {
                    String sku = parts[0].trim();
                    String name = parts[1].trim();
                    BigDecimal price = new BigDecimal(parts[2].trim());
                    int stock = Integer.parseInt(parts[3].trim());

                    if (sku.isEmpty() || name.isEmpty()) {
                        errors.add("Line " + lineNo + ": sku and name cannot be empty");
                        continue;
                    }
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("Line " + lineNo + ": price must be greater than 0");
                        continue;
                    }
                    if (stock < 0) {
                        errors.add("Line " + lineNo + ": stock cannot be negative");
                        continue;
                    }
                    if (repo.existsBySku(sku)) {
                        skipped++;
                        continue;
                    }

                    Product p = new Product();
                    p.setSku(sku);
                    p.setName(name);
                    p.setPrice(price);
                    p.setStock(stock);
                    repo.save(p);
                    imported++;

                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNo + ": price or stock is not a valid number");
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the file");
        }

        return new ImportResult(imported, skipped, errors);
    }
}