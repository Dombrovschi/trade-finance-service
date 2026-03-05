package com.andreidombrovschi.tradefinance.traderequest.repository;

import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRequestHistoryRepository extends JpaRepository<TradeRequestHistory, Long> {
    List<TradeRequestHistory> findByTradeRequestIdOrderByHappenedAtAsc(Long tradeRequestId);
}