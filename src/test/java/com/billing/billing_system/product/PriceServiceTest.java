package com.billing.billing_system.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private ProductRepository productRepo;

    @Mock
    private PriceHistoryRepository historyRepo;

    @InjectMocks
    private PriceService priceService;

    private Product rice() {
        Product p = new Product();
        p.setId(1L);
        p.setSku("RICE01");
        p.setName("Basmati Rice 1kg");
        p.setPrice(new BigDecimal("95.50"));
        p.setStock(40);
        return p;
    }

    @Test
    @DisplayName("Price change saves the new price and logs old price, new price and who changed it")
    void updatesPriceAndWritesHistory() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice()));
        when(productRepo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = priceService.updatePrice(1L, new BigDecimal("100"), "admin");

        assertThat(result.getPrice().toPlainString()).isEqualTo("100.00");

        ArgumentCaptor<PriceHistory> captor = ArgumentCaptor.forClass(PriceHistory.class);
        verify(historyRepo).save(captor.capture());
        PriceHistory log = captor.getValue();

        assertThat(log.getProductId()).isEqualTo(1L);
        assertThat(log.getOldPrice()).isEqualByComparingTo("95.50");
        assertThat(log.getNewPrice()).isEqualByComparingTo("100.00");
        assertThat(log.getChangedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Same price (95.5 equals 95.50) is rejected and nothing is logged")
    void rejectsSamePrice() {
        when(productRepo.findById(1L)).thenReturn(Optional.of(rice()));

        assertThatThrownBy(() -> priceService.updatePrice(1L, new BigDecimal("95.5"), "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("same as the current price");

        verify(historyRepo, never()).save(any(PriceHistory.class));
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Unknown product gives a 'Product not found' error")
    void rejectsUnknownProduct() {
        when(productRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceService.updatePrice(99L, new BigDecimal("10.00"), "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Product not found");

        verify(historyRepo, never()).save(any(PriceHistory.class));
    }
}