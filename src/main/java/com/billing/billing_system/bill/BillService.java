package com.billing.billing_system.bill;

import com.billing.billing_system.product.Product;
import com.billing.billing_system.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepo;
    private final ProductRepository productRepo;

    public BillService(BillRepository billRepo, ProductRepository productRepo) {
        this.billRepo = billRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public Bill create(BillRequest request) {
        Bill bill = new Bill();
        bill.setCreatedAt(LocalDateTime.now());
        bill.setPaymentMode(request.paymentMode());
        bill.setCustomerName(request.customerName());
        bill.setCustomerPhone(request.customerPhone());
        bill.setCustomerGstin(request.customerGstin());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal gstTotal = BigDecimal.ZERO;

        for (BillRequest.ItemRequest req : request.items()) {

            Product product = productRepo.findById(req.productId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found: id " + req.productId()));

            if (product.getStock() < req.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Not enough stock for " + product.getName()
                        + " (available: " + product.getStock() + ")");
            }

            product.setStock(product.getStock() - req.quantity());
            productRepo.save(product);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(req.quantity()));
            BigDecimal gstAmount = lineTotal
                    .multiply(product.getGstPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BillItem item = new BillItem();
            item.setBill(bill);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(req.quantity());
            item.setUnitPrice(product.getPrice());
            item.setGstPercent(product.getGstPercent());
            item.setLineTotal(lineTotal);
            item.setGstAmount(gstAmount);
            bill.getItems().add(item);

            subtotal = subtotal.add(lineTotal);
            gstTotal = gstTotal.add(gstAmount);
        }

        bill.setSubtotal(subtotal);
        bill.setGstTotal(gstTotal);
        bill.setTotalAmount(subtotal.add(gstTotal));

        Bill saved = billRepo.save(bill);
        saved.setBillNumber("BILL-" + String.format("%06d", saved.getId()));
        return saved;
    }

    public List<Bill> getAll() {
        return billRepo.findAll();
    }

    public Bill getById(Long id) {
        return billRepo.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
    }
}