package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.ArtigoRequest;
import com.notoriusfight.api.dto.ArtigoAtualizacaoRequest;
import com.notoriusfight.api.dto.ArtigoResponse;
import com.notoriusfight.api.dto.MovimentacaoEstoqueRequest;
import com.notoriusfight.api.dto.PaginaResponse;
import com.notoriusfight.api.dto.StatusRequest;
import com.notoriusfight.api.mapper.ArtigoMapper;
import com.notoriusfight.application.ArtigoService;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.domain.Status;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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
    public PaginaResponse<ArtigoResponse> pesquisar(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(required = false) Boolean abaixoEstoqueMinimo,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return PaginaResponse.de(
                service.pesquisar(
                        descricao,
                        status,
                        categoriaId,
                        fornecedorId,
                        abaixoEstoqueMinimo,
                        pageable),
                mapper::toResponse);
    }

    @PutMapping("/{id}")
    public ArtigoResponse alterar(
            @PathVariable Long id,
            @Valid @RequestBody ArtigoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(
                id,
                request.descricao(),
                request.valorUnitario(),
                request.estoqueMinimo(),
                request.categoriaId(),
                request.fornecedorId()));
    }

    @PutMapping("/{id}/status")
    public ArtigoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @PostMapping("/{id}/estoque/entradas")
    public ArtigoResponse receberEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.receberEstoque(id, request.quantidade()));
    }

    @PostMapping("/{id}/estoque/saidas")
    public ArtigoResponse retirarEstoque(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.retirarEstoque(id, request.quantidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
