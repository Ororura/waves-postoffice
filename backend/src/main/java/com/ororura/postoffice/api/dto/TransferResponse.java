package com.ororura.postoffice.api.dto;

public record TransferResponse(
        int id,
        String from,
        String to,
        long amount,
        int lifeTime,
        String status) {}
