package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.CategoriaRequest;
import com.notoriusfight.api.dto.CategoriaAtualizacaoRequest;
import com.notoriusfight.api.dto.CategoriaResponse;
import com.notoriusfight.api.dto.PaginaResponse;
import com.notoriusfight.api.dto.StatusRequest;
import com.notoriusfight.api.exception.ApiError;
import com.notoriusfight.api.mapper.CategoriaMapper;
import com.notoriusfight.application.CategoriaService;
import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
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

@Tag(name = "Categoria", description = "Classificação, consulta e situação dos artigos")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaMapper mapper;

    public CategoriaController(CategoriaService service, CategoriaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar categoria", description = "Cria uma categoria ativa e devolve sua localização.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Já existe categoria com o mesmo nome",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CategoriaResponse> cadastrar(
            @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = service.cadastrar(request.nome());
        URI location = URI.create("/api/categorias/" + categoria.getId());
        return ResponseEntity.created(location).body(mapper.toResponse(categoria));
    }

    @Operation(summary = "Consultar categoria por ID",
            description = "Retorna os dados usados na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar categorias",
            description = "Combina filtros opcionais e devolve uma página ordenável.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<CategoriaResponse> pesquisar(
            @Parameter(description = "Trecho do nome, sem diferenciar maiúsculas de minúsculas",
                    example = "luva")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Status exato da categoria", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return PaginaResponse.de(
                service.pesquisar(nome, status, pageable),
                mapper::toResponse);
    }

    @Operation(summary = "Alterar categoria",
            description = "Substitui os campos editáveis da categoria. O status possui operação própria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria alterada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public CategoriaResponse alterar(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome()));
    }

    @Operation(summary = "Ativar ou inativar categoria",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public CategoriaResponse alterarStatus(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(summary = "Excluir categoria",
            description = "Exclui fisicamente somente quando nenhum artigo utiliza a categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Categoria em uso por artigos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
