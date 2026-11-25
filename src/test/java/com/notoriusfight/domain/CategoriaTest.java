package com.notoriusfight.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaTest {

    @Test
    void deveAdicionarArtigoEManejarOsDoisLadosDaAssociacao() {
        Categoria categoria = new Categoria("Luvas");
        Artigo artigo = novoArtigo("7890000000001");

        categoria.adicionarArtigo(artigo);

        assertEquals(1, categoria.getArtigos().size());
        assertSame(artigo, categoria.getArtigos().getFirst());
        assertSame(categoria, artigo.getCategoria());
    }

    @Test
    void naoDeveAdicionarArtigoNulo() {
        Categoria categoria = new Categoria("Luvas");

        assertThrows(NullPointerException.class, () -> categoria.adicionarArtigo(null));
    }

    @Test
    void naoDeveAdicionarDoisArtigosComOMesmoCodigo() {
        Categoria categoria = new Categoria("Luvas");
        categoria.adicionarArtigo(novoArtigo("7890000000001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> categoria.adicionarArtigo(novoArtigo("7890000000001")));

        assertEquals("SKU já utilizado na categoria", excecao.getMessage());
    }

    @Test
    void naoDevePermitirQueArtigoPertençaADoisCategorias() {
        Categoria luvas = new Categoria("Luvas");
        Categoria materialEscolar = new Categoria("Material escolar");
        Artigo artigo = novoArtigo("7890000000001");
        luvas.adicionarArtigo(artigo);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> materialEscolar.adicionarArtigo(artigo));

        assertEquals("Artigo já pertence a outra categoria", excecao.getMessage());
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        Categoria categoria = new Categoria("Luvas");
        Artigo artigo = novoArtigo("7890000000001");
        categoria.adicionarArtigo(artigo);

        assertThrows(
                UnsupportedOperationException.class,
                () -> categoria.getArtigos().add(novoArtigo("7890000000002")));
    }

    private Artigo novoArtigo(String sku) {
        return new Artigo(
                sku,
                "Luva",
                new BigDecimal("3.000"),
                new BigDecimal("12.90"),
                LocalDate.of(2026, 8, 20));
    }
}
