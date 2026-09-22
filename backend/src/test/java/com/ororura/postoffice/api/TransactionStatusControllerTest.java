package com.ororura.postoffice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.infrastructure.blockchain.TransactionStatusNotAvailableException;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionStatusControllerTest {
    private final WavesNodeClient node = mock(WavesNodeClient.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new TransactionStatusController(node)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void unknownOrUnminedStatusIsRetryableAndNotA502() throws Exception {
        when(node.transactionStatus("tx1")).thenThrow(new TransactionStatusNotAvailableException());
        mvc.perform(get("/api/v1/transactions/tx1/status"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Retry-After", "2"))
                .andExpect(jsonPath("$.transactionId").value("tx1"))
                .andExpect(jsonPath("$.state").value("NOT_AVAILABLE_YET"));
    }

    @Test
    void emptyArrayIsRetryable() throws Exception {
        when(node.transactionStatus("tx1")).thenReturn(mapper.readTree("[]"));
        mvc.perform(get("/api/v1/transactions/tx1/status"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.state").value("NOT_AVAILABLE_YET"));
    }

    @Test
    void successIsConfirmedByExecutionNotBroadcast() throws Exception {
        when(node.transactionStatus("tx1")).thenReturn(mapper.readTree("[{\"status\":\"Success\"}]"));
        mvc.perform(get("/api/v1/transactions/tx1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("SUCCESS"))
                .andExpect(jsonPath("$.executions[0].status").value("Success"));
    }

    @Test
    void failedExecutionIsNotReportedAsSuccess() throws Exception {
        when(node.transactionStatus("tx1")).thenReturn(mapper.readTree("[{\"status\":\"Error\"}]"));
        mvc.perform(get("/api/v1/transactions/tx1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("FAILED"));
    }
}
