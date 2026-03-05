package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class VatCalculationServiceTest {

    private TradeRequestRepository repo;
    private VatCalculationService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TradeRequestRepository.class);
        service = new VatCalculationService(repo);
    }

    @Test
    void shouldCalculate45PercentVat() {
        TradeRequest trade = new TradeRequest();
        trade.setStatus(TradeStatus.EXPORTER_ACCEPTED);
        trade.setFinalPrice(10000L);

        when(repo.findByStatus(TradeStatus.EXPORTER_ACCEPTED)).thenReturn(List.of(trade));

        service.calculateVatForApprovedTransactions();

        assertEquals(4500L, trade.getVatAmount());
    }

    @Test
    void shouldSkipTradesWithNullPrice() {
        TradeRequest trade = new TradeRequest();
        trade.setStatus(TradeStatus.EXPORTER_ACCEPTED);
        trade.setFinalPrice(null);

        when(repo.findByStatus(TradeStatus.EXPORTER_ACCEPTED)).thenReturn(List.of(trade));

        service.calculateVatForApprovedTransactions();

        assertNull(trade.getVatAmount());
    }

    @Test
    void shouldCalculateVatForMultipleTrades() {
        TradeRequest t1 = new TradeRequest();
        t1.setFinalPrice(1000L);

        TradeRequest t2 = new TradeRequest();
        t2.setFinalPrice(2000L);

        when(repo.findByStatus(TradeStatus.EXPORTER_ACCEPTED)).thenReturn(List.of(t1, t2));

        service.calculateVatForApprovedTransactions();

        assertEquals(450L, t1.getVatAmount());
        assertEquals(900L, t2.getVatAmount());
    }

    @Test
    void shouldHandleEmptyList() {
        when(repo.findByStatus(TradeStatus.EXPORTER_ACCEPTED)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.calculateVatForApprovedTransactions());
    }

    private void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Expected no exception but got: " + e.getMessage(), e);
        }
    }
}
