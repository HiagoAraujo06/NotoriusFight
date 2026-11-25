package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        Status status) {
}
