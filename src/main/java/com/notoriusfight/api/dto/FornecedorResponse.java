package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um fornecedor")
public record FornecedorResponse(
        @Schema(description = "Identificador do fornecedor", example = "1")
        Long id,
        @Schema(description = "Razão social", example = "Arena Distribuidora Ltda.")
        String razaoSocial,
        @Schema(description = "CNPJ com 14 dígitos", example = "12345678000195")
        String cnpj,
        @Schema(description = "Situação do fornecedor", example = "ATIVO")
        Status status) {
}
