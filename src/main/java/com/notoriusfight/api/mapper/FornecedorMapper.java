package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.FornecedorRequest;
import com.notoriusfight.api.dto.FornecedorResponse;
import com.notoriusfight.domain.Fornecedor;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public Fornecedor toEntity(FornecedorRequest request) {
        return new Fornecedor(request.razaoSocial(), request.cnpj());
    }

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getRazaoSocial(),
                fornecedor.getCnpj(),
                fornecedor.getStatus());
    }
}
