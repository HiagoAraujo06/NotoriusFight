package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.ClienteResponse;
import com.notoriusfight.domain.Cliente;
import com.notoriusfight.domain.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public ClienteResponse toResponse(Cliente cliente) {
        Pessoa pessoa = cliente.getPessoa();
        return new ClienteResponse(cliente.getId(), pessoa.getId(), pessoa.getNome(), pessoa.getEmail(),
                pessoa.getCpf(), cliente.getTelefone(), pessoa.getStatus());
    }
}
