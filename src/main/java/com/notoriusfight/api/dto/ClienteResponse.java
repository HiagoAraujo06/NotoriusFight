package com.notoriusfight.api.dto;

import com.notoriusfight.domain.Status;

public record ClienteResponse(Long id, Long pessoaId, String nome, String email,
                              String cpf, String telefone, Status status) { }
