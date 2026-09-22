package com.ororura.postoffice.infrastructure.blockchain;

import com.ororura.postoffice.config.BlockchainProperties;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class WavesNodeTransactionStatusTest {
    @Test
    void node404IsSpecialStatusUnavailableException() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:6862");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://127.0.0.1:6862/contracts/status/tx1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        WavesNodeClient node = new WavesNodeClient(builder.build(), properties());
        assertThrows(TransactionStatusNotAvailableException.class, () -> node.transactionStatus("tx1"));
        server.verify();
    }

    @Test
    void node500IsNotSilentlyReclassifiedAsPending() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://127.0.0.1:6862");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://127.0.0.1:6862/contracts/status/tx1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        WavesNodeClient node = new WavesNodeClient(builder.build(), properties());
        assertThrows(HttpServerErrorException.class, () -> node.transactionStatus("tx1"));
        server.verify();
    }

    private BlockchainProperties properties() {
        return new BlockchainProperties(
                URI.create("http://127.0.0.1:6862"), "contract123", "",
                "owner", URI.create("http://127.0.0.1:6862"), "",
                "recipient", URI.create("http://127.0.0.1:6882"), "");
    }
}
