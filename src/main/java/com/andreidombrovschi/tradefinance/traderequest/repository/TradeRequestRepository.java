package com.andreidombrovschi.tradefinance.traderequest.repository;

import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    List<TradeRequest> findByImporterId(Long importerId);

    List<TradeRequest> findByBankIdAndStatusIn(Long bankId, List<TradeStatus> statuses);

    List<TradeRequest> findByExporterIdAndStatus(Long exporterId, TradeStatus status);

    List<TradeRequest> findByStatus(TradeStatus status);
}
