package com.andreidombrovschi.tradefinance.traderequest.service;

import com.andreidombrovschi.tradefinance.party.model.PartyType;
import com.andreidombrovschi.tradefinance.party.service.PartyService;
import com.andreidombrovschi.tradefinance.traderequest.dto.CreateTradeRequestDto;
import com.andreidombrovschi.tradefinance.traderequest.model.TradeRequest;
import com.andreidombrovschi.tradefinance.traderequest.repository.TradeRequestRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class TradeRequestServiceTest {

    @Test
    void shouldCreateTradeRequest() {

        TradeRequestRepository repo = Mockito.mock(TradeRequestRepository.class);
        PartyService partyService = Mockito.mock(PartyService.class);
        TradeRequestHistoryService historyService = Mockito.mock(TradeRequestHistoryService.class);

        TradeRequestService service = new TradeRequestService(repo, partyService, historyService);

        CreateTradeRequestDto dto = new CreateTradeRequestDto();
        dto.setImporterId(1L);
        dto.setBankId(2L);
        dto.setExporterId(3L);
        dto.setGoodsDescription("Phones");
        dto.setQuantity(100);
        dto.setCurrency("USD");

        Mockito.when(partyService.exists(1L, PartyType.IMPORTER)).thenReturn(true);
        Mockito.when(partyService.exists(2L, PartyType.BANK)).thenReturn(true);
        Mockito.when(partyService.exists(3L, PartyType.EXPORTER)).thenReturn(true);

        Mockito.when(repo.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.createTradeRequest(dto);

        assertEquals("Phones", result.getGoodsDescription());
        assertEquals(100, result.getQuantity());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void shouldApproveTradeRequest() {

        TradeRequestRepository repo = Mockito.mock(TradeRequestRepository.class);
        PartyService partyService = Mockito.mock(PartyService.class);
        TradeRequestHistoryService historyService = Mockito.mock(TradeRequestHistoryService.class);

        TradeRequestService service = new TradeRequestService(repo, partyService, historyService);

        TradeRequest trade = new TradeRequest();
        trade.setId(1L);
        trade.setBankId(2L);
        trade.setStatus(com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus.SUBMITTED);

        Mockito.when(repo.findById(1L)).thenReturn(java.util.Optional.of(trade));
        Mockito.when(repo.save(Mockito.any())).thenAnswer(i -> i.getArguments()[0]);

        TradeRequest result = service.approveTradeRequest(1L, 2L);

        assertEquals(
                com.andreidombrovschi.tradefinance.traderequest.model.TradeStatus.BANK_APPROVED,
                result.getStatus()
        );
    }
}