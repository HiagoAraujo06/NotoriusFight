package com.notoriusfight.api.dto;

import com.notoriusfight.domain.FuncaoColaborador;
import com.notoriusfight.domain.Status;

import java.time.LocalDate;

public record ColaboradorResponse(Long id, Long pessoaId, String nome, String email,
                                  String cpf, Status status, String matricula,
                                  FuncaoColaborador funcao, LocalDate dataAdmissao) { }
