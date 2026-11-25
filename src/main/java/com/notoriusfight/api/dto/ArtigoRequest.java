package com.notoriusfight.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ArtigoRequest(
        @NotBlank(message = "SKU é obrigatório")
        @Size(max = 50, message = "SKU deve possuir no máximo 50 caracteres")
        String sku,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150, message = "Descrição deve possuir no máximo 150 caracteres")
        String descricao,

        @NotNull(message = "Saldo de estoque é obrigatório")
        @PositiveOrZero(message = "Saldo de estoque não pode ser negativo")
        BigDecimal saldoEstoque,

        @NotNull(message = "Valor unitário é obrigatório")
        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        BigDecimal estoqueMinimo,

        @NotNull(message = "Categoria é obrigatória")
        @Positive(message = "Identificador da categoria deve ser positivo")
        Long categoriaId,

        @Positive(message = "Identificador do fornecedor deve ser positivo")
        Long fornecedorId) {
}
