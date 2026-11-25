package com.notoriusfight.api.mapper;

import com.notoriusfight.api.dto.CategoriaResponse;
import com.notoriusfight.domain.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getStatus());
    }
}
