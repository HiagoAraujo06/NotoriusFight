package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;

public record CategoriaResponse(
        Long id,
        String nome,
        Status status) {
}
