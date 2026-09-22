package com.ororura.postoffice.infrastructure.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.api.dto.Actor;
import com.ororura.postoffice.api.dto.TransactionAccepted;
import com.ororura.postoffice.config.BlockchainProperties;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WavesTransactionClientTest {
    @Test
    void usesUpdatedContractVersionAndCorrectNodeForRecipient() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WavesNodeClient node = mock(WavesNodeClient.class);
        when(node.contractInfo()).thenReturn(mapper.readTree("{\"version\":2}"));
        BlockchainProperties properties = new BlockchainProperties(
                URI.create("http://127.0.0.1:6862"), "contract123", "",
                "ownerAddress", URI.create("http://127.0.0.1:6862"), "ownerSecret",
                "recipientAddress", URI.create("http://127.0.0.1:6882"), "recipientSecret");

        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://127.0.0.1:6882/transactions/signAndBroadcast"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "type":104,
                          "version":2,
                          "contractVersion":2,
                          "contractId":"contract123",
                          "sender":"recipientAddress",
                          "params":[
                            {"type":"string","key":"action","value":"acceptTransfer"},
                            {"type":"integer","key":"id","value":1}
                          ]
                        }
                        """, false))
                .andRespond(withSuccess("{\"id\":\"tx123\"}", MediaType.APPLICATION_JSON));

        WavesTransactionClient client = new WavesTransactionClient(properties, node, mapper, builder);
        TransactionAccepted result = client.call(
                Actor.RECIPIENT, "acceptTransfer", List.of(ContractParameter.integer("id", 1)));
        assertEquals("tx123", result.transactionId());
        assertEquals("/api/v1/transactions/tx123/status", result.statusUrl());
        server.verify();
    }
}
