package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VatCalculationService {

    private final TradeRequestRepository repo;

    public VatCalculationService(TradeRequestRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void calculateVatForApprovedTransactions() {
        List<TradeRequest> acceptedTrades = repo.findByStatus(TradeStatus.EXPORTER_ACCEPTED);
        for (TradeRequest trade : acceptedTrades) {
            if (trade.getFinalPrice() != null) {
                long vat = Math.round(trade.getFinalPrice() * 0.45);
                trade.setVatAmount(vat);
            }
        }
        repo.saveAll(acceptedTrades);
    }
}