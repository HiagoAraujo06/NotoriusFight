package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ArtigoResponse(
        Long id,
        String sku,
        String descricao,
        BigDecimal saldoEstoque,
        BigDecimal valorUnitario,
        BigDecimal estoqueMinimo,
        BigDecimal valorEstoque,
        LocalDate dataCadastro,
        Status status,
        Long categoriaId,
        String categoriaNome,
        Long fornecedorId,
        String fornecedorRazaoSocial) {
}
