package com.ororura.postoffice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTransferRequest(
        @NotNull Actor actor,
        @NotBlank String to,
        @Positive long amount) {}
