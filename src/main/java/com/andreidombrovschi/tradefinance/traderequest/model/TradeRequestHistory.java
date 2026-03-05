package com.andreidombrovschi.tradefinance.traderequest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "trade_request_history")
@Getter
@Setter
@NoArgsConstructor
public class TradeRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tradeRequestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TradeAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeStatus toStatus;

    @Column(nullable = false)
    private Long actorPartyId;

    @Column(nullable = false, length = 20)
    private String actorPartyType; // IMPORTER / BANK / EXPORTER

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant happenedAt;

    @PrePersist
    void prePersist() {
        happenedAt = Instant.now();
    }
}