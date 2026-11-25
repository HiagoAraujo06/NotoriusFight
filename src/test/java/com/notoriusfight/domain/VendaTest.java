package com.notoriusfight.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VendaTest {
    @Test
    void deveCalcularTotalComPrecoCapturadoNoMomentoDaInclusao() {
        Venda venda = novaVenda();
        Artigo artigo = novoArtigo();
        venda.adicionarItem(artigo, new BigDecimal("2.000"));

        artigo.alterarValorUnitario(new BigDecimal("15.00"));

        assertEquals(new BigDecimal("20.00"), venda.calcularTotal());
        venda.finalizar();
        assertEquals(StatusVenda.FINALIZADA, venda.getStatus());
    }

    @Test
    void naoDeveAlterarVendaFinalizada() {
        Venda venda = novaVenda();
        venda.adicionarItem(novoArtigo(), BigDecimal.ONE);
        venda.finalizar();
        assertThrows(IllegalStateException.class, venda::cancelar);
    }

    private Venda novaVenda() {
        Pessoa pessoaCliente = new Pessoa("Cliente", "cliente@example.com", "52998224725", LocalDate.now());
        Pessoa pessoaVendedor = new Pessoa("Vendedor", "vendedor@example.com", "11144477735", LocalDate.now());
        return new Venda(new Cliente(pessoaCliente, null),
                new Colaborador(pessoaVendedor, "VEN-1", FuncaoColaborador.VENDEDOR, LocalDate.now()),
                LocalDateTime.of(2026, 9, 29, 10, 0));
    }

    private Artigo novoArtigo() {
        return new Artigo("P-1", "Artigo", new BigDecimal("10.000"), new BigDecimal("10.00"), LocalDate.now());
    }
}
