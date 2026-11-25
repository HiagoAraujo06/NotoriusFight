package com.notoriusfight.application;

import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Status;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final ArtigoRepository artigoRepository;

    public CategoriaService(
            CategoriaRepository repository,
            ArtigoRepository artigoRepository) {
        this.repository = repository;
        this.artigoRepository = artigoRepository;
    }

    @Transactional
    public Categoria cadastrar(String nome) {
        String nomeNormalizado = normalizarNome(nome);
        if (repository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new RecursoDuplicadoException("Nome da categoria já cadastrado");
        }
        return repository.save(new Categoria(nomeNormalizado));
    }

    @Transactional
    public Categoria alterar(Long id, String nome) {
        String nomeNormalizado = normalizarNome(nome);
        if (repository.existsByNomeIgnoreCaseAndIdNot(nomeNormalizado, id)) {
            throw new RecursoDuplicadoException("Nome da categoria já cadastrado");
        }

        Categoria categoria = buscarEntidade(id);
        categoria.alterarNome(nomeNormalizado);
        return categoria;
    }

    @Transactional
    public Categoria alterarStatus(Long id, Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
        Categoria categoria = buscarEntidade(id);
        if (status == Status.ATIVO) {
            categoria.ativar();
        } else {
            categoria.inativar();
        }
        return categoria;
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (artigoRepository.existsByCategoriaId(id)) {
            throw new RecursoEmUsoException(
                    "Categoria não pode ser excluída porque possui artigos");
        }
        repository.delete(categoria);
        repository.flush();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return buscarEntidade(id);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Categoria> pesquisar(
            String nome,
            Status status,
            Pageable pageable) {
        validarOrdenacao(pageable, Set.of("id", "nome", "status"));
        Specification<Categoria> filtros = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            filtros = filtros.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), trecho));
        }
        if (status != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return repository.findAll(filtros, pageable);
    }

    private Categoria buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada"));
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
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
