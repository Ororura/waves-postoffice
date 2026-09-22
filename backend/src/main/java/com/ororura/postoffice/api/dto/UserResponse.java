package com.ororura.postoffice.api.dto;

public record UserResponse(
        String name,
        String homeAddress,
        String blockchainAddress,
        long balance,
        String role,
        String postId) {}
