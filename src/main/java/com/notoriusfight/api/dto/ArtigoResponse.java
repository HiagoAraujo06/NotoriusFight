package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Modalidade;
import com.notoriusfight.domain.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representação completa de um artigo")
public record ArtigoResponse(
        @Schema(description = "Identificador do artigo", example = "1")
        Long id,
        @Schema(description = "Código único do artigo", example = "7890000000001")
        String sku,
        @Schema(description = "Descrição comercial", example = "Luva de boxe 12 oz")
        String descricao,
        @Schema(description = "Saldo atual", example = "10.000")
        BigDecimal saldoEstoque,
        @Schema(description = "Preço atual por unidade", example = "18.90")
        BigDecimal valorUnitario,
        @Schema(description = "Limite usado para indicar reposição", example = "3.000")
        BigDecimal estoqueMinimo,
        @Schema(description = "Saldo multiplicado pelo valor unitário", example = "189.00")
        BigDecimal valorEstoque,
        @Schema(description = "Data de entrada do artigo no cadastro", example = "2026-09-29")
        LocalDate dataCadastro,
        @Schema(description = "Situação do artigo", example = "ATIVO")
        Status status,
        @Schema(description = "Marca do artigo", example = "NotoriusFight")
        String marca,
        @Schema(description = "Arte marcial do artigo", example = "BOXE")
        Modalidade modalidade,
        @Schema(description = "Tamanho, peso ou numeração", example = "12 OZ")
        String tamanho,
        @Schema(description = "Identificador da categoria", example = "1")
        Long categoriaId,
        @Schema(description = "Nome da categoria", example = "Luvas")
        String categoriaNome,
        @Schema(description = "Identificador do fornecedor, quando associado", example = "1",
                nullable = true)
        Long fornecedorId,
        @Schema(description = "Razão social do fornecedor, quando associado",
                example = "Arena Distribuidora Ltda.", nullable = true)
        String fornecedorRazaoSocial) {
}
