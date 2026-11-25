package com.notoriusfight.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtigoTest {

    @Test
    void deveCriarArtigoAtivoComDadosValidos() {
        Artigo artigo = novoArtigo("3.000", "12.90");

        assertEquals("7890000000001", artigo.getSku());
        assertEquals("Luva", artigo.getDescricao());
        assertEquals(Status.ATIVO, artigo.getStatus());
        assertEquals(LocalDate.of(2026, 8, 20), artigo.getDataCadastro());
    }

    @Test
    void deveCalcularValorDoEstoque() {
        Artigo artigo = novoArtigo("3.000", "12.90");

        BigDecimal valorEstoque = artigo.calcularValorEstoque();

        assertEquals(0, new BigDecimal("38.70").compareTo(valorEstoque));
    }

    @Test
    void deveReceberERetirarEstoque() {
        Artigo artigo = novoArtigo("3.000", "12.90");

        artigo.receberEstoque(new BigDecimal("2.500"));
        artigo.retirarEstoque(new BigDecimal("1.000"));

        assertEquals(0, new BigDecimal("4.500").compareTo(artigo.getSaldoEstoque()));
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() {
        Artigo artigo = novoArtigo("3.000", "12.90");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> artigo.retirarEstoque(new BigDecimal("3.001")));

        assertEquals("Saldo de estoque insuficiente", excecao.getMessage());
    }

    @Test
    void naoDeveCriarArtigoComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Artigo(
                        "  ",
                        "Luva",
                        BigDecimal.ZERO,
                        new BigDecimal("12.90"),
                        LocalDate.of(2026, 8, 20)));
    }

    @Test
    void naoDeveCriarArtigoComSaldoNegativo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> novoArtigo("-0.001", "12.90"));
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        Artigo artigo = novoArtigo("3.000", "12.90");

        artigo.inativar();
        assertEquals(Status.INATIVO, artigo.getStatus());

        artigo.ativar();
        assertEquals(Status.ATIVO, artigo.getStatus());
    }

    private Artigo novoArtigo(String saldo, String valorUnitario) {
        return new Artigo(
                "7890000000001",
                "Luva",
                new BigDecimal(saldo),
                new BigDecimal(valorUnitario),
                LocalDate.of(2026, 8, 20));
    }
}
