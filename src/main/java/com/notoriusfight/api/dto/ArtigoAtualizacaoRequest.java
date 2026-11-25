package com.notoriusfight.api.dto;

import com.notoriusfight.api.validation.QuantidadeInteira;
import com.notoriusfight.domain.Modalidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Campos substituíveis de um artigo; código, saldo e status possuem fluxos próprios")
public record ArtigoAtualizacaoRequest(
        @Schema(description = "Descrição comercial", example = "Luva de boxe Pro 14 oz")
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150, message = "Descrição deve possuir no máximo 150 caracteres")
        String descricao,

        @Schema(description = "Preço atual por unidade", example = "21.90")
        @NotNull(message = "Valor unitário é obrigatório")
        @PositiveOrZero(message = "Valor unitário não pode ser negativo")
        BigDecimal valorUnitario,

        @Schema(description = "Limite usado para indicar reposição", example = "5.000")
        @NotNull(message = "Estoque mínimo é obrigatório")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo")
        @Digits(integer = 15, fraction = 3)
        @QuantidadeInteira(message = "Estoque mínimo deve ser inteiro")
        BigDecimal estoqueMinimo,

        @Schema(description = "Marca do artigo", example = "NotoriusFight")
        @NotBlank(message = "Marca é obrigatória")
        @Size(max = 80, message = "Marca deve possuir no máximo 80 caracteres")
        String marca,

        @Schema(description = "Arte marcial do artigo", example = "BOXE")
        @NotNull(message = "Modalidade é obrigatória")
        Modalidade modalidade,

        @Schema(description = "Tamanho, peso ou numeração", example = "12 OZ")
        @NotBlank(message = "Tamanho é obrigatório")
        @Size(max = 20, message = "Tamanho deve possuir no máximo 20 caracteres")
        String tamanho,

        @Schema(description = "Identificador de uma categoria existente", example = "1")
        @NotNull(message = "Categoria é obrigatória")
        @Positive(message = "Identificador da categoria deve ser positivo")
        Long categoriaId,

        @Schema(description = "Identificador opcional de um fornecedor existente", example = "1",
                nullable = true)
        @Positive(message = "Identificador do fornecedor deve ser positivo")
        Long fornecedorId) {
}
