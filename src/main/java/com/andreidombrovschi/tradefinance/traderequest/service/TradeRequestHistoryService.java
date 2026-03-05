package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.traderequest.model.*;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeRequestHistoryService {

    private final TradeRequestHistoryRepository repo;

    public TradeRequestHistoryService(TradeRequestHistoryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void record(
            Long tradeRequestId,
            TradeAction action,
            TradeStatus fromStatus,
            TradeStatus toStatus,
            Long actorPartyId,
            String actorPartyType,
            String comment
    ) {
        TradeRequestHistory h = new TradeRequestHistory();
        h.setTradeRequestId(tradeRequestId);
        h.setAction(action);
        h.setFromStatus(fromStatus);
        h.setToStatus(toStatus);
        h.setActorPartyId(actorPartyId);
        h.setActorPartyType(actorPartyType);
        h.setComment(comment);
        repo.save(h);
    }

    @Transactional(readOnly = true)
    public List<TradeRequestHistory> listByTradeRequestId(Long tradeRequestId) {
        return repo.findByTradeRequestIdOrderByHappenedAtAsc(tradeRequestId);
    }
}