package com.notoriusfight.application;

import com.notoriusfight.domain.Fornecedor;
import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.repository.FornecedorRepository;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArtigoServiceTest {

    @Autowired
    private ArtigoService artigoService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ArtigoRepository artigoRepository;

    @Test
    void deveCadastrarArtigoComCategoriaEFornecedor() {
        Categoria categoria = categoriaRepository.save(new Categoria("Kimonos de teste"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor de teste",
                "11111111000191"));

        Artigo cadastrado = artigoService.cadastrar(
                novoArtigo("TESTE-SERVICE-001"),
                categoria.getId(),
                fornecedor.getId());

        assertNotNull(cadastrado.getId());
        assertEquals(categoria.getId(), cadastrado.getCategoria().getId());
        assertEquals(fornecedor.getId(), cadastrado.getFornecedor().getId());
    }

    @Test
    void deveImpedirSkuDuplicado() {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria duplicidade"));
        artigoService.cadastrar(novoArtigo("TESTE-DUPLICADO"), categoria.getId(), null);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> artigoService.cadastrar(
                        novoArtigo("TESTE-DUPLICADO"),
                        categoria.getId(),
                        null));
    }

    @Test
    void deveInformarCategoriaInexistenteSemSalvarArtigo() {
        Artigo artigo = novoArtigo("TESTE-SEM-GRUPO");

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> artigoService.cadastrar(artigo, Long.MAX_VALUE, null));
        assertEquals(false, artigoRepository.existsBySku("TESTE-SEM-GRUPO"));
    }

    @Test
    void deveAtualizarSaldoPorDirtyChecking() {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria dirty checking"));
        Artigo artigo = artigoService.cadastrar(
                novoArtigo("TESTE-DIRTY-CHECKING"),
                categoria.getId(),
                null);

        artigoService.receberEstoque(artigo.getId(), new BigDecimal("5.000"));

        Artigo atualizado = artigoRepository.findById(artigo.getId()).orElseThrow();
        assertEquals(0, new BigDecimal("15.000").compareTo(atualizado.getSaldoEstoque()));
    }

    private Artigo novoArtigo(String codigo) {
        return new Artigo(
                codigo,
                "Artigo de teste",
                new BigDecimal("10.000"),
                new BigDecimal("25.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 8, 27));
    }
}
