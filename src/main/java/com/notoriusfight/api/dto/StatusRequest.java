package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull(message = "Status é obrigatório")
        Status status) {
}
