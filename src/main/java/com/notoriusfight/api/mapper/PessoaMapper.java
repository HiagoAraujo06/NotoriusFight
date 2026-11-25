package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.PessoaRequest;
import com.notoriusfight.api.dto.PessoaResponse;
import com.notoriusfight.domain.Pessoa;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PessoaMapper {
    public Pessoa toEntity(PessoaRequest request) {
        return new Pessoa(request.nome(), request.email(), request.cpf(), LocalDate.now());
    }

    public PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(pessoa.getId(), pessoa.getNome(), pessoa.getEmail(), pessoa.getCpf(),
                pessoa.getDataCadastro(), pessoa.getStatus());
    }
}
