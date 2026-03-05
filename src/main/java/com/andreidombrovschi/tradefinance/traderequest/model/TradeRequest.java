package com.andreidombrovschi.tradefinance.traderequest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "trade_request")
@Getter
@Setter
@NoArgsConstructor
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // references to Party (we store IDs for now)
    @Column(nullable = false)
    private Long importerId;

    @Column(nullable = false)
    private Long bankId;

    @Column(nullable = false)
    private Long exporterId;

    @Column(nullable = false, length = 500)
    private String goodsDescription;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TradeStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // set by exporter on accept
    private Long finalPrice;

    @Column(length = 500)
    private String deliveryDetails;

    // set by exporter on decline (optional)
    @Column(length = 300)
    private String exporterDeclineReason;

    @Version
    private Long version;

    @Column
    private Long vatAmount;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) status = TradeStatus.SUBMITTED;
    }
}