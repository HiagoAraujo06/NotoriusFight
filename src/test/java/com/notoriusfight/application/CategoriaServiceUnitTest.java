package com.notoriusfight.application;

import com.notoriusfight.domain.Categoria;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceUnitTest {

    @Mock
    private CategoriaRepository repository;

    @Mock
    private ArtigoRepository artigoRepository;

    @InjectMocks
    private CategoriaService service;

    @Test
    void deveNormalizarNomeAoAlterar() {
        Categoria categoria = new Categoria("Nome original");
        when(repository.existsByNomeIgnoreCaseAndIdNot("Nome alterado", 1L))
                .thenReturn(false);
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria alterado = service.alterar(1L, "  Nome alterado  ");

        assertEquals("Nome alterado", alterado.getNome());
        verify(repository).existsByNomeIgnoreCaseAndIdNot("Nome alterado", 1L);
    }

    @Test
    void naoDeveExcluirCategoriaQuePossuiArtigos() {
        Categoria categoria = new Categoria("Categoria em uso");
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(artigoRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThrows(RecursoEmUsoException.class, () -> service.excluir(1L));
    }

    @Test
    void deveRejeitarOrdenacaoNaoPermitida() {
        PageRequest pagina = PageRequest.of(0, 20).withSort(
                org.springframework.data.domain.Sort.by("artigos.descricao"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pesquisar(null, null, pagina));
    }
}
