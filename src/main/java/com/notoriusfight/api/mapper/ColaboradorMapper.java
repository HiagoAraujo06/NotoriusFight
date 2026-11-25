package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.ColaboradorResponse;
import com.notoriusfight.domain.Colaborador;
import com.notoriusfight.domain.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class ColaboradorMapper {
    public ColaboradorResponse toResponse(Colaborador colaborador) {
        Pessoa pessoa = colaborador.getPessoa();
        return new ColaboradorResponse(colaborador.getId(), pessoa.getId(), pessoa.getNome(), pessoa.getEmail(),
                pessoa.getCpf(), pessoa.getStatus(), colaborador.getMatricula(), colaborador.getFuncao(),
                colaborador.getDataAdmissao());
    }
}
