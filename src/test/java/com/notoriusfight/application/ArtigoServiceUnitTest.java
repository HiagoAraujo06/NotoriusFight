package com.notoriusfight.application;

import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.repository.FornecedorRepository;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtigoServiceUnitTest {

    @Mock
    private ArtigoRepository artigoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ArtigoService service;

    @Test
    void deveAlterarDadosECategoriaDoArtigo() {
        Categoria categoriaOriginal = new Categoria("Categoria original");
        Categoria novaCategoria = new Categoria("Nova categoria");
        Artigo artigo = novoArtigo();
        categoriaOriginal.adicionarArtigo(artigo);
        when(artigoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(artigo));
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(novaCategoria));

        Artigo alterado = service.alterar(
                1L,
                "Descrição alterada",
                new BigDecimal("75.90"),
                new BigDecimal("3.000"),
                2L,
                null);

        assertEquals("Descrição alterada", alterado.getDescricao());
        assertEquals(novaCategoria, alterado.getCategoria());
        assertEquals(0, categoriaOriginal.getArtigos().size());
        assertEquals(1, novaCategoria.getArtigos().size());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() {
        Artigo artigo = novoArtigo();
        when(artigoRepository.buscarPorIdComRelacionamentos(1L))
                .thenReturn(Optional.of(artigo));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.retirarEstoque(1L, new BigDecimal("10.001")));
        assertEquals(0, new BigDecimal("10.000").compareTo(artigo.getSaldoEstoque()));
    }

    @Test
    void deveRejeitarOrdenacaoNaoPermitida() {
        PageRequest pagina = PageRequest.of(
                0,
                20,
                Sort.by("fornecedor.razaoSocial"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pesquisar(null, null, null, null, null, pagina));
    }

    private Artigo novoArtigo() {
        return new Artigo(
                "UNIT-001",
                "Artigo unitário",
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
    }
}
