package com.notoriusfight.application;

import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ArtigoService {

    private final ArtigoRepository artigoRepository;
    private final CategoriaRepository categoriaRepository;

    public ArtigoService(
            ArtigoRepository artigoRepository,
            CategoriaRepository categoriaRepository) {
        this.artigoRepository = artigoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Artigo cadastrar(Artigo artigo, Long categoriaId) {
        if (artigoRepository.existsBySku(artigo.getSku())) {
            throw new RecursoDuplicadoException("SKU já cadastrado");
        }

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada"));
        categoria.adicionarArtigo(artigo);

        return artigoRepository.save(artigo);
    }

    @Transactional(readOnly = true)
    public Artigo buscarPorId(Long id) {
        return artigoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Artigo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Artigo> listar() {
        return artigoRepository.buscarTodosComRelacionamentos();
    }

    @Transactional
    public Artigo receberEstoque(Long id, BigDecimal quantidade) {
        Artigo artigo = artigoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Artigo não encontrado"));
        artigo.receberEstoque(quantidade);
        return artigo;
    }
}
