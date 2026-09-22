package com.ororura.postoffice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.ororura.postoffice.api.dto.TransactionStatusResponse;
import com.ororura.postoffice.infrastructure.blockchain.TransactionStatusNotAvailableException;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionStatusController {
    private final WavesNodeClient node;

    public TransactionStatusController(WavesNodeClient node) {
        this.node = node;
    }

    @GetMapping("/{transactionId}/status")
    public ResponseEntity<TransactionStatusResponse> status(@PathVariable String transactionId) {
        final JsonNode executions;
        try {
            executions = node.transactionStatus(transactionId);
        } catch (TransactionStatusNotAvailableException exception) {
            // 404 from the node means no execution record currently exists for this ID.
            // It may be too early, or the ID may simply be unknown.
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header("Retry-After", "2")
                    .body(new TransactionStatusResponse(transactionId, "NOT_AVAILABLE_YET", null));
        }
        if (executions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header("Retry-After", "2")
                    .body(new TransactionStatusResponse(transactionId, "NOT_AVAILABLE_YET", executions));
        }
        boolean allSuccessful = true;
        for (JsonNode execution : executions) {
            String upstreamStatus = execution.path("status").asText("").toLowerCase(Locale.ROOT);
            if (upstreamStatus.equals("error") || upstreamStatus.equals("failed")
                    || upstreamStatus.equals("failure")) {
                return ResponseEntity.ok(new TransactionStatusResponse(transactionId, "FAILED", executions));
            }
            if (!upstreamStatus.equals("success")) {
                allSuccessful = false;
            }
        }
        if (!allSuccessful) {
            // A returned execution with an unfamiliar status must not be mistaken for success.
            return ResponseEntity.ok(new TransactionStatusResponse(transactionId, "UNRECOGNIZED", executions));
        }
        return ResponseEntity.ok(new TransactionStatusResponse(transactionId, "SUCCESS", executions));
    }
}
