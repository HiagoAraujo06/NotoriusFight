package com.notoriusfight.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos substituíveis de uma categoria")
public record CategoriaAtualizacaoRequest(
        @Schema(description = "Novo nome único da categoria", example = "Material de escritório")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve possuir no máximo 120 caracteres")
        String nome) {
}
