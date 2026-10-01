package com.billing.billing_system.product;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceService {

    private final ProductRepository productRepo;
    private final PriceHistoryRepository historyRepo;

    public PriceService(ProductRepository productRepo, PriceHistoryRepository historyRepo) {
        this.productRepo = productRepo;
        this.historyRepo = historyRepo;
    }

    @Transactional
    public Product updatePrice(Long productId, BigDecimal newPrice, String changedBy) {
        Product product = productRepo.findById(productId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        BigDecimal price = newPrice.setScale(2, RoundingMode.HALF_UP);

        if (product.getPrice().compareTo(price) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New price is the same as the current price");
        }

        PriceHistory history = new PriceHistory();
        history.setProductId(product.getId());
        history.setOldPrice(product.getPrice());
        history.setNewPrice(price);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        historyRepo.save(history);

        product.setPrice(price);
        return productRepo.save(product);
    }

    public List<PriceHistory> getHistory(Long productId) {
        if (!productRepo.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return historyRepo.findByProductIdOrderByIdDesc(productId);
    }
}