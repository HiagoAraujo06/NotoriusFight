package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de uma categoria")
public record CategoriaResponse(
        @Schema(description = "Identificador da categoria", example = "1")
        Long id,
        @Schema(description = "Nome da categoria", example = "Luvas")
        String nome,
        @Schema(description = "Situação da categoria", example = "ATIVO")
        Status status) {
}
