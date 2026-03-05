package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.common.exceptions.ConflictException;
import com.andreidombrovschi.tradefinance.common.exceptions.ErrorCode;
import com.andreidombrovschi.tradefinance.common.exceptions.ForbiddenException;
import com.andreidombrovschi.tradefinance.common.exceptions.NotFoundException;
import com.andreidombrovschi.tradefinance.party.model.PartyType;
import com.andreidombrovschi.tradefinance.party.service.PartyService;
import com.andreidombrovschi.tradefinance.traderequest.dto.CreateTradeRequestDto;
import com.andreidombrovschi.tradefinance.traderequest.dto.UpdateTradeRequestDto;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeAction;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TradeRequestService {

    private final TradeRequestRepository repo;
    private final PartyService partyService;
    private final TradeRequestHistoryService historyService;

    public TradeRequestService(TradeRequestRepository repo, PartyService partyService, TradeRequestHistoryService historyService) {
        this.repo = repo;
        this.partyService = partyService;
        this.historyService = historyService;
    }

    @Transactional
    public TradeRequest createTradeRequest(CreateTradeRequestDto dto) {
        // validate parties exist and are correct types
        if (!partyService.exists(dto.getImporterId(), PartyType.IMPORTER)) {
            throw new ConflictException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid importerId (must exist and be an IMPORTER)"
            );
        }

        if (!partyService.exists(dto.getBankId(), PartyType.BANK)) {
            throw new ConflictException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid bankId (must exist and be a BANK)"
            );
        }

        if (!partyService.exists(dto.getExporterId(), PartyType.EXPORTER)) {
            throw new ConflictException(
                    ErrorCode.VALIDATION_ERROR,
                    "Invalid exporterId (must exist and be an EXPORTER)"
            );
        }

        TradeRequest tr = new TradeRequest();
        tr.setImporterId(dto.getImporterId());
        tr.setBankId(dto.getBankId());
        tr.setExporterId(dto.getExporterId());
        tr.setGoodsDescription(dto.getGoodsDescription());
        tr.setQuantity(dto.getQuantity());
        tr.setCurrency(dto.getCurrency());

        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.CREATED,
                TradeStatus.SUBMITTED,
                TradeStatus.SUBMITTED,
                dto.getImporterId(),
                "IMPORTER",
                "Trade request created"
        );

        return saved;
    }

    @Transactional
    public TradeRequest approveTradeRequest(Long tradeRequestId, Long actingBankId) {
        TradeRequest tr = repo.findById(tradeRequestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRADE_REQUEST_NOT_FOUND,
                        "TradeRequest not found: " + tradeRequestId
                ));

        if (!tr.getBankId().equals(actingBankId)) {
            throw new ForbiddenException(
                    ErrorCode.WRONG_ACTOR,
                    "This bank cannot approve this trade request"
            );
        }

        if (tr.getStatus() != TradeStatus.SUBMITTED && tr.getStatus() != TradeStatus.EXPORTER_DECLINED) {
            throw new ConflictException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only SUBMITTED or EXPORTER_DECLINED requests can be approved"
            );
        }

        TradeStatus from = tr.getStatus();
        tr.setStatus(TradeStatus.BANK_APPROVED);
        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.BANK_APPROVED,
                from,
                saved.getStatus(),
                actingBankId,
                "BANK",
                null
        );

        return saved;
    }

    @Transactional
    public TradeRequest rejectTradeRequest(Long tradeRequestId, Long actingBankId, String reason) {
        TradeRequest tr = repo.findById(tradeRequestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRADE_REQUEST_NOT_FOUND,
                        "TradeRequest not found: " + tradeRequestId
                ));

        if (!tr.getBankId().equals(actingBankId)) {
            throw new ForbiddenException(
                    ErrorCode.WRONG_ACTOR,
                    "This bank cannot reject this trade request"
            );
        }

        if (tr.getStatus() != TradeStatus.SUBMITTED && tr.getStatus() != TradeStatus.EXPORTER_DECLINED) {
            throw new ConflictException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only SUBMITTED or EXPORTER_DECLINED requests can be rejected"
            );
        }

        TradeStatus from = tr.getStatus();
        tr.setStatus(TradeStatus.BANK_REJECTED);
        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.BANK_REJECTED,
                from,
                saved.getStatus(),
                actingBankId,
                "BANK",
                reason
        );

        return saved;
    }

    @Transactional
    public TradeRequest exporterAccept(Long tradeRequestId, Long actingExporterId, Long price, String deliveryDetails) {
        TradeRequest tr = repo.findById(tradeRequestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRADE_REQUEST_NOT_FOUND,
                        "TradeRequest not found: " + tradeRequestId
                ));

        if (!tr.getExporterId().equals(actingExporterId)) {
            throw new ForbiddenException(ErrorCode.WRONG_ACTOR, "This exporter cannot accept this trade request");
        }

        if (tr.getStatus() != TradeStatus.BANK_APPROVED) {
            throw new ConflictException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only BANK_APPROVED requests can be accepted");
        }

        TradeStatus from = tr.getStatus();

        tr.setFinalPrice(price);
        tr.setDeliveryDetails(deliveryDetails);
        tr.setExporterDeclineReason(null);
        tr.setStatus(TradeStatus.EXPORTER_ACCEPTED);

        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.EXPORTER_ACCEPTED,
                from,
                saved.getStatus(),
                actingExporterId,
                "EXPORTER",
                deliveryDetails
        );

        return saved;
    }

    @Transactional
    public TradeRequest exporterDecline(Long tradeRequestId, Long actingExporterId, String reason) {
        TradeRequest tr = repo.findById(tradeRequestId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRADE_REQUEST_NOT_FOUND,
                        "TradeRequest not found: " + tradeRequestId
                ));

        if (!tr.getExporterId().equals(actingExporterId)) {
            throw new ForbiddenException(ErrorCode.WRONG_ACTOR, "This exporter cannot decline this trade request");
        }

        if (tr.getStatus() != TradeStatus.BANK_APPROVED) {
            throw new ConflictException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only BANK_APPROVED requests can be declined");
        }

        TradeStatus from = tr.getStatus();

        tr.setExporterDeclineReason(reason);
        tr.setFinalPrice(null);
        tr.setDeliveryDetails(null);
        tr.setStatus(TradeStatus.EXPORTER_DECLINED);

        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.EXPORTER_DECLINED,
                from,
                saved.getStatus(),
                actingExporterId,
                "EXPORTER",
                reason
        );

        return saved;
    }

    @Transactional
    public TradeRequest importerUpdateAndResubmit(Long id, Long actingImporterId, UpdateTradeRequestDto dto) {

        TradeRequest tr = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRADE_REQUEST_NOT_FOUND,
                        "TradeRequest not found: " + id
                ));

        if (!tr.getImporterId().equals(actingImporterId)) {
            throw new ForbiddenException(ErrorCode.WRONG_ACTOR, "This importer cannot modify this trade request");
        }

        if (tr.getStatus() != TradeStatus.BANK_REJECTED) {
            throw new ConflictException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Only BANK_REJECTED requests can be edited/resubmitted"
            );
        }

        if (!partyService.exists(dto.getBankId(), PartyType.BANK)) {
            throw new ConflictException(ErrorCode.VALIDATION_ERROR, "Invalid bankId");
        }

        if (!partyService.exists(dto.getExporterId(), PartyType.EXPORTER)) {
            throw new ConflictException(ErrorCode.VALIDATION_ERROR, "Invalid exporterId");
        }

        TradeStatus from = tr.getStatus();

        tr.setGoodsDescription(dto.getGoodsDescription());
        tr.setQuantity(dto.getQuantity());
        tr.setCurrency(dto.getCurrency());
        tr.setBankId(dto.getBankId());
        tr.setExporterId(dto.getExporterId());

        tr.setStatus(TradeStatus.SUBMITTED);

        TradeRequest saved = repo.save(tr);

        historyService.record(
                saved.getId(),
                TradeAction.RESUBMITTED,
                from,
                saved.getStatus(),
                actingImporterId,
                "IMPORTER",
                "Corrected and resubmitted"
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TradeRequest> getImporterRequests(Long importerId) {
        return repo.findByImporterId(importerId);
    }

    @Transactional(readOnly = true)
    public List<TradeRequest> getBankRequests(Long bankId) {

        return repo.findByBankIdAndStatusIn(
                bankId,
                List.of(
                        TradeStatus.SUBMITTED,
                        TradeStatus.EXPORTER_DECLINED
                )
        );
    }

    @Transactional(readOnly = true)
    public List<TradeRequest> getExporterRequests(Long exporterId) {

        return repo.findByExporterIdAndStatus(
                exporterId,
                TradeStatus.BANK_APPROVED
        );
    }

}