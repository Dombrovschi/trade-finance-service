package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.common.exceptions.ConflictException;
import com.andreidombrovschi.tradefinance.common.exceptions.ForbiddenException;
import com.andreidombrovschi.tradefinance.common.exceptions.NotFoundException;
import com.andreidombrovschi.tradefinance.party.model.PartyType;
import com.andreidombrovschi.tradefinance.party.service.PartyService;
import com.andreidombrovschi.tradefinance.traderequest.dto.CreateTradeRequestDto;
import com.andreidombrovschi.tradefinance.traderequest.dto.UpdateTradeRequestDto;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TradeRequestServiceTest {

    private TradeRequestRepository repo;
    private PartyService partyService;
    private TradeRequestService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(TradeRequestRepository.class);
        partyService = Mockito.mock(PartyService.class);
        TradeRequestHistoryService historyService = Mockito.mock(TradeRequestHistoryService.class);
        service = new TradeRequestService(repo, partyService, historyService);
    }

    // ─── createTradeRequest ───────────────────────────────────────────────────

    @Test
    void shouldCreateTradeRequest() {
        when(partyService.exists(1L, PartyType.IMPORTER)).thenReturn(true);
        when(partyService.exists(2L, PartyType.BANK)).thenReturn(true);
        when(partyService.exists(3L, PartyType.EXPORTER)).thenReturn(true);
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.createTradeRequest(buildCreateDto());

        assertEquals("Phones", result.getGoodsDescription());
        assertEquals(100, result.getQuantity());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void shouldThrowWhenImporterInvalid() {
        when(partyService.exists(1L, PartyType.IMPORTER)).thenReturn(false);

        assertThrows(ConflictException.class, () -> service.createTradeRequest(buildCreateDto()));
    }

    @Test
    void shouldThrowWhenBankInvalid() {
        when(partyService.exists(1L, PartyType.IMPORTER)).thenReturn(true);
        when(partyService.exists(2L, PartyType.BANK)).thenReturn(false);

        assertThrows(ConflictException.class, () -> service.createTradeRequest(buildCreateDto()));
    }

    @Test
    void shouldThrowWhenExporterInvalid() {
        when(partyService.exists(1L, PartyType.IMPORTER)).thenReturn(true);
        when(partyService.exists(2L, PartyType.BANK)).thenReturn(true);
        when(partyService.exists(3L, PartyType.EXPORTER)).thenReturn(false);

        assertThrows(ConflictException.class, () -> service.createTradeRequest(buildCreateDto()));
    }

    // ─── approveTradeRequest ─────────────────────────────────────────────────

    @Test
    void shouldApproveTradeRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.SUBMITTED, 2L, null)));
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.approveTradeRequest(1L, 2L);

        assertEquals(TradeStatus.BANK_APPROVED, result.getStatus());
    }

    @Test
    void shouldApproveWhenExporterDeclined() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.EXPORTER_DECLINED, 2L, null)));
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.approveTradeRequest(1L, 2L);

        assertEquals(TradeStatus.BANK_APPROVED, result.getStatus());
    }

    @Test
    void shouldThrowWhenWrongBankApproves() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.SUBMITTED, 2L, null)));

        assertThrows(ForbiddenException.class, () -> service.approveTradeRequest(1L, 99L));
    }

    @Test
    void shouldThrowWhenApprovingNonSubmittedRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, null)));

        assertThrows(ConflictException.class, () -> service.approveTradeRequest(1L, 2L));
    }

    @Test
    void shouldThrowWhenApproveTradeNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.approveTradeRequest(99L, 2L));
    }

    // ─── rejectTradeRequest ──────────────────────────────────────────────────

    @Test
    void shouldRejectTradeRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.SUBMITTED, 2L, null)));
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.rejectTradeRequest(1L, 2L, "Missing docs");

        assertEquals(TradeStatus.BANK_REJECTED, result.getStatus());
    }

    @Test
    void shouldThrowWhenWrongBankRejects() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.SUBMITTED, 2L, null)));

        assertThrows(ForbiddenException.class, () -> service.rejectTradeRequest(1L, 99L, "reason"));
    }

    @Test
    void shouldThrowWhenRejectingAlreadyApprovedRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, null)));

        assertThrows(ConflictException.class, () -> service.rejectTradeRequest(1L, 2L, "reason"));
    }

    // ─── exporterAccept ──────────────────────────────────────────────────────

    @Test
    void shouldExporterAccept() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, 3L)));
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.exporterAccept(1L, 3L, 5000L, "DHL, 30 days");

        assertEquals(TradeStatus.EXPORTER_ACCEPTED, result.getStatus());
        assertEquals(5000L, result.getFinalPrice());
        assertEquals("DHL, 30 days", result.getDeliveryDetails());
    }

    @Test
    void shouldThrowWhenWrongExporterAccepts() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, 3L)));

        assertThrows(ForbiddenException.class, () -> service.exporterAccept(1L, 99L, 5000L, "DHL"));
    }

    @Test
    void shouldThrowWhenAcceptingNotApprovedRequest() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.SUBMITTED, 2L, 3L)));

        assertThrows(ConflictException.class, () -> service.exporterAccept(1L, 3L, 5000L, "DHL"));
    }

    // ─── exporterDecline ─────────────────────────────────────────────────────

    @Test
    void shouldExporterDecline() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, 3L)));
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.exporterDecline(1L, 3L, "No capacity");

        assertEquals(TradeStatus.EXPORTER_DECLINED, result.getStatus());
        assertEquals("No capacity", result.getExporterDeclineReason());
    }

    @Test
    void shouldThrowWhenWrongExporterDeclines() {
        when(repo.findById(1L)).thenReturn(Optional.of(tradeWith(TradeStatus.BANK_APPROVED, 2L, 3L)));

        assertThrows(ForbiddenException.class, () -> service.exporterDecline(1L, 99L, "reason"));
    }

    // ─── importerUpdateAndResubmit ───────────────────────────────────────────

    @Test
    void shouldResubmitAfterBankRejection() {
        TradeRequest trade = tradeWith(TradeStatus.BANK_REJECTED, 2L, 3L);
        trade.setImporterId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(trade));
        when(partyService.exists(2L, PartyType.BANK)).thenReturn(true);
        when(partyService.exists(5L, PartyType.EXPORTER)).thenReturn(true);
        when(repo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UpdateTradeRequestDto dto = new UpdateTradeRequestDto();
        dto.setGoodsDescription("Updated goods");
        dto.setQuantity(200);
        dto.setCurrency("EUR");
        dto.setBankId(2L);
        dto.setExporterId(5L);

        TradeRequest result = service.importerUpdateAndResubmit(1L, 1L, dto);

        assertEquals(TradeStatus.SUBMITTED, result.getStatus());
        assertEquals("Updated goods", result.getGoodsDescription());
    }

    @Test
    void shouldThrowWhenResubmittingNonRejectedRequest() {
        TradeRequest trade = tradeWith(TradeStatus.SUBMITTED, 2L, 3L);
        trade.setImporterId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(trade));

        UpdateTradeRequestDto dto = new UpdateTradeRequestDto();
        dto.setGoodsDescription("x");
        dto.setQuantity(1);
        dto.setCurrency("EUR");
        dto.setBankId(2L);
        dto.setExporterId(3L);

        assertThrows(ConflictException.class, () -> service.importerUpdateAndResubmit(1L, 1L, dto));
    }

    @Test
    void shouldThrowWhenWrongImporterResubmits() {
        TradeRequest trade = tradeWith(TradeStatus.BANK_REJECTED, 2L, 3L);
        trade.setImporterId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(trade));

        UpdateTradeRequestDto dto = new UpdateTradeRequestDto();
        dto.setGoodsDescription("x");
        dto.setQuantity(1);
        dto.setCurrency("EUR");
        dto.setBankId(2L);
        dto.setExporterId(3L);

        assertThrows(ForbiddenException.class, () -> service.importerUpdateAndResubmit(1L, 99L, dto));
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private CreateTradeRequestDto buildCreateDto() {
        CreateTradeRequestDto dto = new CreateTradeRequestDto();
        dto.setImporterId(1L);
        dto.setBankId(2L);
        dto.setExporterId(3L);
        dto.setGoodsDescription("Phones");
        dto.setQuantity(100);
        dto.setCurrency("USD");
        return dto;
    }

    private TradeRequest tradeWith(TradeStatus status, Long bankId, Long exporterId) {
        TradeRequest tr = new TradeRequest();
        tr.setId(1L);
        tr.setStatus(status);
        tr.setBankId(bankId);
        tr.setExporterId(exporterId);
        tr.setImporterId(1L);
        return tr;
    }
}