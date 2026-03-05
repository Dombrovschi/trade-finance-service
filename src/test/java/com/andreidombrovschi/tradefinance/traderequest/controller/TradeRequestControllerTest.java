package com.andreidombrovschi.tradefinance.traderequest.controller;

import com.andreidombrovschi.tradefinance.traderequest.service.TradeRequestHistoryService;
import com.andreidombrovschi.tradefinance.traderequest.service.TradeRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TradeRequestControllerTest {

    @Test
    void shouldReturnImporterRequests() throws Exception {

        TradeRequestService service = Mockito.mock(TradeRequestService.class);
        TradeRequestHistoryService historyService = Mockito.mock(TradeRequestHistoryService.class);

        TradeRequestController controller =
                new TradeRequestController(service, historyService);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mockMvc.perform(
                get("/trade-requests/importer")
                        .header("X-Party-Id", "1")
                        .header("X-Party-Type", "IMPORTER")
        ).andExpect(status().isOk());
    }
}