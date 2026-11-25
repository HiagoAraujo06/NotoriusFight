package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.ArtigoRequest;
import com.notoriusfight.api.dto.ArtigoAtualizacaoRequest;
import com.notoriusfight.api.dto.ArtigoResponse;
import com.notoriusfight.api.dto.MovimentacaoEstoqueRequest;
import com.notoriusfight.api.dto.PaginaResponse;
import com.notoriusfight.api.dto.StatusRequest;
import com.notoriusfight.api.exception.ApiError;
import com.notoriusfight.api.mapper.ArtigoMapper;
import com.notoriusfight.application.ArtigoService;
import com.notoriusfight.domain.Artigo;
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

@Tag(name = "Artigos", description = "Cadastro, consulta e movimentação de estoque")
@RestController
@RequestMapping("/api/artigos")
public class ArtigoController {

    private final ArtigoService service;
    private final ArtigoMapper mapper;

    public ArtigoController(ArtigoService service, ArtigoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar artigo",
            description = "Cria um artigo ativo associado a uma categoria e, opcionalmente, a um fornecedor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Artigo cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ou fornecedor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "SKU já utilizado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Consultar artigo por ID",
            description = "Retorna a representação completa usada na visualização e na tela de alteração.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artigo encontrado"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ArtigoResponse buscarPorId(
            @Parameter(description = "Identificador do artigo", example = "1")
            @PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Pesquisar artigos",
            description = "Combina filtros opcionais e executa paginação e ordenação no banco de dados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa executada"),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<ArtigoResponse> pesquisar(
            @Parameter(description = "Trecho da descrição, sem diferenciar maiúsculas de minúsculas",
                    example = "luva")
            @RequestParam(required = false) String descricao,
            @Parameter(description = "Status exato do artigo", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Identificador da categoria", example = "1")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Identificador do fornecedor", example = "1")
            @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "Quando verdadeiro, retorna artigos com saldo abaixo do mínimo",
                    example = "true")
            @RequestParam(required = false) Boolean abaixoEstoqueMinimo,
            @Parameter(description = "Parâmetros page, size e sort. A página começa em zero.")
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

    @Operation(summary = "Alterar artigo",
            description = "Substitui descrição, preço, estoque mínimo, categoria e fornecedor. "
                    + "Código, saldo e status possuem fluxos próprios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artigo alterado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Artigo, categoria ou fornecedor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Operação incompatível com a integridade dos dados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ArtigoResponse alterar(
            @Parameter(description = "Identificador do artigo", example = "1")
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

    @Operation(summary = "Ativar ou inativar artigo",
            description = "Executa uma transição explícita de status sem alterar os demais campos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public ArtigoResponse alterarStatus(
            @Parameter(description = "Identificador do artigo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(summary = "Receber estoque",
            description = "Bloqueia o artigo e soma uma quantidade positiva ao saldo atual. Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/entradas")
    public ArtigoResponse receberEstoque(
            @Parameter(description = "Identificador do artigo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.receberEstoque(id, request.quantidade()));
    }

    @Operation(summary = "Retirar estoque",
            description = "Bloqueia o artigo e subtrai uma quantidade positiva sem permitir saldo negativo. Repetir a chamada repete o efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saída registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida ou saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/saidas")
    public ArtigoResponse retirarEstoque(
            @Parameter(description = "Identificador do artigo", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.retirarEstoque(id, request.quantidade()));
    }

    @Operation(summary = "Excluir artigo", description = "Exclui fisicamente o artigo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Artigo excluído"),
            @ApiResponse(responseCode = "404", description = "Artigo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Artigo referenciado por outro registro",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do artigo", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
