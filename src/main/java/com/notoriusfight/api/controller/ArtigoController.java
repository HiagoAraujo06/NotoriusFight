package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.ArtigoRequest;
import com.notoriusfight.api.dto.ArtigoResponse;
import com.notoriusfight.api.mapper.ArtigoMapper;
import com.notoriusfight.application.ArtigoService;
import com.notoriusfight.domain.Artigo;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/artigos")
public class ArtigoController {

    private final ArtigoService service;
    private final ArtigoMapper mapper;

    public ArtigoController(ArtigoService service, ArtigoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ArtigoResponse> cadastrar(
            @Valid @RequestBody ArtigoRequest request) {
        Artigo artigo = mapper.toEntity(request);
        Artigo cadastrado = service.cadastrar(
                artigo,
                request.categoriaId(),
                request.fornecedorId());
        URI location = URI.create("/api/artigos/" + cadastrado.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(cadastrado));
    }

    @GetMapping("/{id}")
    public ArtigoResponse buscarPorId(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @GetMapping
    public List<ArtigoResponse> listar() {
        return service.listar().stream().map(mapper::toResponse).toList();
    }
}
