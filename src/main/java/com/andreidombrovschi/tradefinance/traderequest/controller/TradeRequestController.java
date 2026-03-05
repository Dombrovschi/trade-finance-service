package com.andreidombrovschi.tradefinance.traderequest.controller;

import com.andreidombrovschi.tradefinance.common.exceptions.ErrorCode;
import com.andreidombrovschi.tradefinance.common.exceptions.ForbiddenException;
import com.andreidombrovschi.tradefinance.traderequest.dto.*;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequestHistory;
import com.andreidombrovschi.tradefinance.traderequest.service.TradeRequestHistoryService;
import com.andreidombrovschi.tradefinance.traderequest.service.TradeRequestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade-requests")
public class TradeRequestController {

    private final TradeRequestService service;
    private final TradeRequestHistoryService historyService;

    public TradeRequestController(TradeRequestService service, TradeRequestHistoryService historyService) {
        this.service = service;
        this.historyService = historyService;
    }

    @PostMapping
    public TradeRequest create(@Valid @RequestBody CreateTradeRequestDto dto) {
        return service.createTradeRequest(dto);
    }

    @PostMapping("/{id}/approve")
    public TradeRequest approve(
            @PathVariable Long id,
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType
    ) {
        if (!"BANK".equalsIgnoreCase(partyType)) {
            throw new IllegalStateException("Only BANK can approve");
        }
        return service.approveTradeRequest(id, partyId);
    }

    @PostMapping("/{id}/reject")
    public TradeRequest reject(
            @PathVariable Long id,
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType,
            @Valid @RequestBody RejectTradeRequestDto dto
    ) {
        if (!"BANK".equalsIgnoreCase(partyType)) {
            throw new IllegalStateException("Only BANK can reject");
        }
        return service.rejectTradeRequest(id, partyId, dto.getReason());
    }

    @PostMapping("/{id}/exporter-accept")
    public TradeRequest exporterAccept(
            @PathVariable Long id,
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType,
            @Valid @RequestBody ExporterAcceptDto dto
    ) {
        if (!"EXPORTER".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ACTION, "Only EXPORTER can accept");
        }
        return service.exporterAccept(id, partyId, dto.getPrice(), dto.getDeliveryDetails());
    }

    @PostMapping("/{id}/exporter-decline")
    public TradeRequest exporterDecline(
            @PathVariable Long id,
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType,
            @Valid @RequestBody ExporterDeclineDto dto
    ) {
        if (!"EXPORTER".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ACTION, "Only EXPORTER can decline");
        }
        return service.exporterDecline(id, partyId, dto.getReason());
    }

    @GetMapping("/{id}/history")
    public List<TradeRequestHistory> history(@PathVariable Long id) {
        return historyService.listByTradeRequestId(id);
    }

    @PutMapping("/{id}")
    public TradeRequest updateAndResubmit(
            @PathVariable Long id,
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType,
            @Valid @RequestBody UpdateTradeRequestDto dto
    ) {

        if (!"IMPORTER".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_ACTION, "Only IMPORTER can edit/resubmit");
        }

        return service.importerUpdateAndResubmit(id, partyId, dto);
    }

    @GetMapping("/importer")
    public List<TradeRequest> importerRequests(
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType
    ) {

        if (!"IMPORTER".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(
                    ErrorCode.FORBIDDEN_ACTION,
                    "Only IMPORTER can view importer requests"
            );
        }

        return service.getImporterRequests(partyId);
    }

    @GetMapping("/bank")
    public List<TradeRequest> bankRequests(
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType
    ) {

        if (!"BANK".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(
                    ErrorCode.FORBIDDEN_ACTION,
                    "Only BANK can view bank requests"
            );
        }

        return service.getBankRequests(partyId);
    }

    @GetMapping("/exporter")
    public List<TradeRequest> exporterRequests(
            @RequestHeader("X-Party-Id") Long partyId,
            @RequestHeader("X-Party-Type") String partyType
    ) {

        if (!"EXPORTER".equalsIgnoreCase(partyType)) {
            throw new ForbiddenException(
                    ErrorCode.FORBIDDEN_ACTION,
                    "Only EXPORTER can view exporter requests"
            );
        }

        return service.getExporterRequests(partyId);
    }

}
