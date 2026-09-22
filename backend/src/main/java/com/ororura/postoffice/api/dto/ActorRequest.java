package com.ororura.postoffice.api.dto;

import jakarta.validation.constraints.NotNull;

public record ActorRequest(@NotNull Actor actor) {}
