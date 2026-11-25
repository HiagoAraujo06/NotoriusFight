package com.notoriusfight.application;

import com.notoriusfight.domain.Fornecedor;
import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.domain.Status;
import com.notoriusfight.repository.FornecedorRepository;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ArtigoService {

    private final ArtigoRepository artigoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final EntityManager entityManager;

    public ArtigoService(
            ArtigoRepository artigoRepository,
            CategoriaRepository categoriaRepository,
            FornecedorRepository fornecedorRepository,
            EntityManager entityManager) {
        this.artigoRepository = artigoRepository;
        this.categoriaRepository = categoriaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Artigo cadastrar(Artigo artigo, Long categoriaId, Long fornecedorId) {
        if (artigoRepository.existsBySku(artigo.getSku())) {
            throw new RecursoDuplicadoException("SKU já cadastrado");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada"));
        categoria.adicionarArtigo(artigo);

        if (fornecedorId != null) {
            Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Fornecedor não encontrado"));
            artigo.associarFornecedor(fornecedor);
        }

        return artigoRepository.save(artigo);
    }

    @Transactional(readOnly = true)
    public Artigo buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional(readOnly = true)
    public List<Artigo> listar() {
        return artigoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Artigo receberEstoque(Long id, BigDecimal quantidade) {
        Artigo artigo = buscarEntidade(id);
        entityManager.refresh(artigo, LockModeType.PESSIMISTIC_WRITE);
        artigo.receberEstoque(quantidade);
        return artigo;
    }

    @Transactional
    public Artigo retirarEstoque(Long id, BigDecimal quantidade) {
        Artigo artigo = buscarEntidade(id);
        entityManager.refresh(artigo, LockModeType.PESSIMISTIC_WRITE);
        artigo.retirarEstoque(quantidade);
        return artigo;
    }

    @Transactional
    public Artigo alterar(
            Long id,
            String descricao,
            BigDecimal valorUnitario,
            BigDecimal estoqueMinimo,
            Long categoriaId,
            Long fornecedorId) {
        Artigo artigo = buscarEntidade(id);
        Categoria categoria = buscarCategoria(categoriaId);
        Fornecedor fornecedor = fornecedorId == null
                ? null
                : buscarFornecedor(fornecedorId);

        artigo.alterarDescricao(descricao);
        artigo.alterarValorUnitario(valorUnitario);
        artigo.alterarEstoqueMinimo(estoqueMinimo);
        artigo.alterarCategoria(categoria);
        artigo.alterarFornecedor(fornecedor);
        return artigo;
    }

    @Transactional
    public Artigo alterarStatus(Long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        Artigo artigo = buscarEntidade(id);
        if (status == Status.ATIVO) {
            artigo.ativar();
        } else {
            artigo.inativar();
        }
        return artigo;
    }

    @Transactional
    public void excluir(Long id) {
        Artigo artigo = buscarEntidade(id);
        artigoRepository.delete(artigo);
        artigoRepository.flush();
    }

    @Transactional(readOnly = true)
    public Page<Artigo> pesquisar(
            String descricao,
            Status status,
            Long categoriaId,
            Long fornecedorId,
            Boolean abaixoEstoqueMinimo,
            Pageable pageable) {
        validarOrdenacao(
                pageable,
                Set.of("id", "sku", "descricao", "saldoEstoque",
                        "valorUnitario", "estoqueMinimo", "dataCadastro", "status"));
        Specification<Artigo> filtros = (root, query, cb) -> cb.conjunction();

        if (descricao != null && !descricao.isBlank()) {
            String trecho = "%" + descricao.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("descricao")), trecho));
        }
        if (status != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }
        if (categoriaId != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (fornecedorId != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("fornecedor").get("id"), fornecedorId));
        }
        if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
            filtros = filtros.and((root, query, cb) ->
                    cb.lessThan(root.get("saldoEstoque"), root.get("estoqueMinimo")));
        }

        return artigoRepository.findAll(filtros, pageable);
    }

    private Artigo buscarEntidade(Long id) {
        return artigoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Artigo não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada"));
    }

    private Fornecedor buscarFornecedor(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Fornecedor não encontrado"));
    }

    private void validarOrdenacao(Pageable pageable, Set<String> camposPermitidos) {
        pageable.getSort().forEach(ordem -> {
            if (!camposPermitidos.contains(ordem.getProperty())) {
                throw new IllegalArgumentException(
                        "Campo de ordenação inválido: " + ordem.getProperty());
            }
        });
    }
}
