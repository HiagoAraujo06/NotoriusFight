package com.notoriusfight.api.dto;

import com.notoriusfight.api.validation.QuantidadeInteira;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Quantidade de uma entrada ou saída de estoque")
public record MovimentacaoEstoqueRequest(
        @Schema(description = "Quantidade positiva a movimentar", example = "5.000")
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        @Digits(integer = 15, fraction = 3)
        @QuantidadeInteira(message = "Quantidade deve ser inteira")
        BigDecimal quantidade) {
}
