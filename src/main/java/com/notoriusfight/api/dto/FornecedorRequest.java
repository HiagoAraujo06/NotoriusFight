package com.notoriusfight.api.dto;

import com.notoriusfight.api.validation.CnpjValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um fornecedor")
public record FornecedorRequest(
        @Schema(description = "Razão social do fornecedor", example = "Arena Distribuidora Ltda.")
        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 150, message = "Razão social deve possuir no máximo 150 caracteres")
        String razaoSocial,

        @Schema(description = "CNPJ numérico ou alfanumérico com 14 posições, sem pontuação", example = "12345678000195")
        @NotBlank(message = "CNPJ é obrigatório")
        @CnpjValido
        String cnpj) {
}
