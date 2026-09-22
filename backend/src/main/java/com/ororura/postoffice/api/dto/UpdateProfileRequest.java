package com.ororura.postoffice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotNull Actor actor,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String homeAddress) {}
