package com.ororura.postoffice.api.dto;

import jakarta.validation.constraints.Positive;

public record CreditRequest(@Positive long amount) {}
