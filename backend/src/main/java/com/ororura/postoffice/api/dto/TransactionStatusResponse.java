package com.ororura.postoffice.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record TransactionStatusResponse(String transactionId, String state, JsonNode executions) {}
