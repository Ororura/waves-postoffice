package com.ororura.postoffice.api;

import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class BlockchainHealthControllerTest {
    @Test
    void returnsBlockchainHeight() throws Exception {
        WavesNodeClient node = mock(WavesNodeClient.class);
        when(node.height()).thenReturn(123L);
        MockMvc mvc = standaloneSetup(new BlockchainHealthController(node))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/v1/health/blockchain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.height").value(123));
    }

    @Test
    void returns503WhenNodeUnavailable() throws Exception {
        WavesNodeClient node = mock(WavesNodeClient.class);
        when(node.height()).thenThrow(new ResourceAccessException("connection refused"));
        MockMvc mvc = standaloneSetup(new BlockchainHealthController(node))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/v1/health/blockchain"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("NODE_UNAVAILABLE"));
    }
}
