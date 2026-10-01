package com.billing.billing_system.bill;

import com.billing.billing_system.product.Product;
import com.billing.billing_system.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepo;

    @Mock
    private ProductRepository productRepo;

    @InjectMocks
    private BillService billService;

    // ----- helpers -----

    private Product product(Long id, String name, String price, int stock, String gst) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU" + id);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setGstPercent(new BigDecimal(gst));
        return p;
    }

    private void saveReturnsSameBill() {
        when(billRepo.save(any(Bill.class))).thenAnswer(inv -> {
            Bill b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });
    }

    // ----- tests -----

    @Test
    @DisplayName("Totals: 2 x 95.50 + 3 x 44.00 with 5% GST gives 339.15")
    void createsBillWithCorrectTotals() {
        Product rice = product(1L, "Basmati Rice 1kg", "95.50", 40, "5.00");
        Product sugar = product(2L, "Sugar 1kg", "44.00", 60, "5.00");
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice));
        when(productRepo.findById(2L)).thenReturn(Optional.of(sugar));
        saveReturnsSameBill();

        BillRequest request = new BillRequest("CASH", null, null, null,
                List.of(new BillRequest.ItemRequest(1L, 2),
                        new BillRequest.ItemRequest(2L, 3)));

        Bill bill = billService.create(request);

        assertThat(bill.getSubtotal()).isEqualByComparingTo("323.00");
        assertThat(bill.getGstTotal()).isEqualByComparingTo("16.15");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("339.15");
        assertThat(bill.getBillNumber()).isEqualTo("BILL-000007");
        assertThat(bill.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Stock is reduced by the quantity sold")
    void reducesStock() {
        Product rice = product(1L, "Basmati Rice 1kg", "95.50", 40, "5.00");
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice));
        saveReturnsSameBill();

        billService.create(new BillRequest("CASH", null, null, null,
                List.of(new BillRequest.ItemRequest(1L, 2))));

        assertThat(rice.getStock()).isEqualTo(38);
    }

    @Test
    @DisplayName("GST is rounded half up: 5% of 95.50 is 4.775, which becomes 4.78")
    void roundsGstHalfUp() {
        Product rice = product(1L, "Basmati Rice 1kg", "95.50", 40, "5.00");
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice));
        saveReturnsSameBill();

        Bill bill = billService.create(new BillRequest("UPI", null, null, null,
                List.of(new BillRequest.ItemRequest(1L, 1))));

        assertThat(bill.getGstTotal()).isEqualByComparingTo("4.78");
        assertThat(bill.getTotalAmount()).isEqualByComparingTo("100.28");
    }

    @Test
    @DisplayName("Not enough stock: error, no bill saved, stock unchanged")
    void rejectsWhenStockIsTooLow() {
        Product rice = product(1L, "Basmati Rice 1kg", "95.50", 5, "5.00");
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice));

        BillRequest request = new BillRequest("CASH", null, null, null,
                List.of(new BillRequest.ItemRequest(1L, 10)));

        assertThatThrownBy(() -> billService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not enough stock");

        verify(billRepo, never()).save(any(Bill.class));
        assertThat(rice.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("Unknown product id gives a 'Product not found' error")
    void rejectsUnknownProduct() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        BillRequest request = new BillRequest("CASH", null, null, null,
                List.of(new BillRequest.ItemRequest(99L, 1)));

        assertThatThrownBy(() -> billService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Product not found");

        verify(billRepo, never()).save(any(Bill.class));
    }

    @Test
    @DisplayName("Customer details and the price at the time of sale are copied onto the bill")
    void copiesCustomerAndPriceOntoBill() {
        Product rice = product(1L, "Basmati Rice 1kg", "95.50", 40, "5.00");
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice));
        saveReturnsSameBill();

        Bill bill = billService.create(new BillRequest("UPI", "Ravi Kumar", "9876543210", null,
                List.of(new BillRequest.ItemRequest(1L, 1))));

        assertThat(bill.getCustomerName()).isEqualTo("Ravi Kumar");
        assertThat(bill.getCustomerPhone()).isEqualTo("9876543210");
        assertThat(bill.getPaymentMode()).isEqualTo("UPI");

        BillItem item = bill.getItems().get(0);
        assertThat(item.getProductName()).isEqualTo("Basmati Rice 1kg");
        assertThat(item.getUnitPrice()).isEqualByComparingTo("95.50");
    }
}