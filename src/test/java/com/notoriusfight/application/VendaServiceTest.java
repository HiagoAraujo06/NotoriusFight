package com.notoriusfight.application;

import com.notoriusfight.domain.*;
import com.notoriusfight.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VendaServiceTest {
    @Autowired VendaService vendaService;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired ColaboradorRepository colaboradorRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ArtigoRepository artigoRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDadosDoCenario() {
        jdbcTemplate.update("DELETE FROM item_venda");
        jdbcTemplate.update("DELETE FROM venda");
        jdbcTemplate.update("DELETE FROM cliente");
        jdbcTemplate.update("DELETE FROM colaborador");
        jdbcTemplate.update("DELETE FROM pessoa");
        jdbcTemplate.update("DELETE FROM artigo WHERE sku LIKE 'VENDA-%'");
        jdbcTemplate.update("DELETE FROM categoria WHERE nome LIKE 'Categoria VENDA-%'");
    }

    @Test
    void deveFinalizarVendaEBaixarEstoque() {
        Participantes participantes = criarParticipantes();
        Artigo artigo = criarArtigo("VENDA-OK-" + System.nanoTime(), "5.000");
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(), participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), artigo.getId(), new BigDecimal("2.000"));

        Venda finalizada = vendaService.finalizar(venda.getId());

        assertEquals(StatusVenda.FINALIZADA, finalizada.getStatus());
        assertEquals(new BigDecimal("3.000"), artigoRepository.findById(artigo.getId()).orElseThrow().getSaldoEstoque());
    }

    @Test
    void deveDesfazerTodasAsBaixasQuandoUmItemNaoTemEstoque() {
        Participantes participantes = criarParticipantes();
        Artigo primeiro = criarArtigo("VENDA-ROLLBACK-A-" + System.nanoTime(), "5.000");
        Artigo segundo = criarArtigo("VENDA-ROLLBACK-B-" + System.nanoTime(), "1.000");
        Venda venda = vendaService.cadastrar(participantes.cliente().getId(), participantes.vendedor().getId());
        vendaService.adicionarItem(venda.getId(), primeiro.getId(), new BigDecimal("2.000"));
        vendaService.adicionarItem(venda.getId(), segundo.getId(), new BigDecimal("2.000"));

        assertThrows(IllegalArgumentException.class, () -> vendaService.finalizar(venda.getId()));

        assertEquals(new BigDecimal("5.000"), artigoRepository.findById(primeiro.getId()).orElseThrow().getSaldoEstoque());
        assertEquals(StatusVenda.ABERTA, vendaService.buscarPorId(venda.getId()).getStatus());
    }

    private Participantes criarParticipantes() {
        long semente = System.nanoTime();
        String sufixo = String.valueOf(semente);
        String cpfCliente = gerarCpf(semente);
        String cpfVendedor = gerarCpf(semente + 7919);
        Pessoa pc = pessoaRepository.save(new Pessoa("Cliente", "cliente" + sufixo + "@example.com", cpfCliente, LocalDate.now()));
        Pessoa pv = pessoaRepository.save(new Pessoa("Vendedor", "vendedor" + sufixo + "@example.com", cpfVendedor, LocalDate.now()));
        Cliente cliente = clienteRepository.save(new Cliente(pc, null));
        Colaborador vendedor = colaboradorRepository.save(new Colaborador(pv, "M-" + sufixo,
                FuncaoColaborador.VENDEDOR, LocalDate.now()));
        return new Participantes(cliente, vendedor);
    }

    private String gerarCpf(long semente) {
        String base = "%09d".formatted(Math.floorMod(semente, 1_000_000_000L));
        if (digitosRepetidos(base)) base = "123456789";
        int primeiro = digitoCpf(base, 10);
        return base + primeiro + digitoCpf(base + primeiro, 11);
    }

    private int digitoCpf(String base, int peso) {
        int soma = 0;
        for (char c : base.toCharArray()) soma += Character.getNumericValue(c) * peso--;
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private boolean digitosRepetidos(String valor) {
        for (int i = 1; i < valor.length(); i++)
            if (valor.charAt(i) != valor.charAt(0)) return false;
        return true;
    }

    private Artigo criarArtigo(String codigo, String saldo) {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria " + codigo));
        Artigo artigo = new Artigo(codigo, "Artigo de teste", new BigDecimal(saldo),
                new BigDecimal("10.00"), LocalDate.now());
        categoria.adicionarArtigo(artigo);
        return artigoRepository.save(artigo);
    }

    private record Participantes(Cliente cliente, Colaborador vendedor) { }
}
