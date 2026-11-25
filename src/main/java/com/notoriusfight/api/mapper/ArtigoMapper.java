package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.ArtigoRequest;
import com.notoriusfight.api.dto.ArtigoResponse;
import com.notoriusfight.domain.Fornecedor;
import com.notoriusfight.domain.Artigo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ArtigoMapper {

    public Artigo toEntity(ArtigoRequest request) {
        return new Artigo(
                request.sku(),
                request.descricao(),
                request.saldoEstoque(),
                request.valorUnitario(),
                request.estoqueMinimo(),
                LocalDate.now());
    }

    public ArtigoResponse toResponse(Artigo artigo) {
        Fornecedor fornecedor = artigo.getFornecedor();

        return new ArtigoResponse(
                artigo.getId(),
                artigo.getSku(),
                artigo.getDescricao(),
                artigo.getSaldoEstoque(),
                artigo.getValorUnitario(),
                artigo.getEstoqueMinimo(),
                artigo.calcularValorEstoque(),
                artigo.getDataCadastro(),
                artigo.getStatus(),
                artigo.getCategoria().getId(),
                artigo.getCategoria().getNome(),
                fornecedor == null ? null : fornecedor.getId(),
                fornecedor == null ? null : fornecedor.getRazaoSocial());
    }
}
