package com.ororura.postoffice.api.dto;

/** Node accepted the transaction; the contract may still fail during execution. */
public record TransactionAccepted(
        String transactionId,
        String contractId,
        String action,
        String statusUrl) {}
