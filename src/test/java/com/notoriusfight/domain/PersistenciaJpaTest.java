package com.notoriusfight.domain;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class PersistenciaJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void devePersistirERelerCategoriaEArtigo() {
        Categoria categoria = new Categoria("Kimonos");
        Artigo artigo = new Artigo(
                "7891000000019",
                "Kimono trançado",
                new BigDecimal("10.000"),
                new BigDecimal("89.90"),
                LocalDate.of(2026, 3, 10));

        categoria.adicionarArtigo(artigo);

        entityManager.persist(categoria);
        entityManager.persist(artigo);
        entityManager.flush();

        Long artigoId = artigo.getId();
        entityManager.clear();

        Artigo artigoRecuperado = entityManager.find(Artigo.class, artigoId);

        assertNotNull(artigoRecuperado);
        assertEquals("Kimono trançado", artigoRecuperado.getDescricao());
        assertEquals("Kimonos", artigoRecuperado.getCategoria().getNome());
        assertEquals(Status.ATIVO, artigoRecuperado.getStatus());
    }

    @Test
    void deveRegistrarTodosOsChangeSets() {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog",
                Integer.class);

        assertEquals(17, quantidade);
    }

    @Test
    @Transactional
    void bancoDeveImpedirSkuDuplicado() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria para unicidade");

        inserirArtigoDiretamente(categoriaId, "CODIGO-REPETIDO", "Primeiro artigo", "1.000", "10.00");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirArtigoDiretamente(
                        categoriaId,
                        "CODIGO-REPETIDO",
                        "Segundo artigo",
                        "1.000",
                        "20.00"));
    }

    @Test
    @Transactional
    void bancoDeveImpedirSaldoNegativo() {
        Long categoriaId = inserirCategoriaDiretamente("Categoria para saldo");

        assertThrows(
                DataIntegrityViolationException.class,
                () -> inserirArtigoDiretamente(
                        categoriaId,
                        "CODIGO-SALDO-NEGATIVO",
                        "Artigo inválido",
                        "-1.000",
                        "10.00"));
    }

    private Long inserirCategoriaDiretamente(String nome) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categoria (nome, status)
                VALUES (?, 'ATIVO')
                RETURNING id
                """,
                Long.class,
                nome);
    }

    private void inserirArtigoDiretamente(
            Long categoriaId,
            String sku,
            String descricao,
            String saldo,
            String valor) {
        jdbcTemplate.update(
                """
                INSERT INTO artigo (
                    sku,
                    descricao,
                    saldo_estoque,
                    valor_unitario,
                    estoque_minimo,
                    data_cadastro,
                    status,
                    categoria_id
                )
                VALUES (?, ?, CAST(? AS NUMERIC), CAST(? AS NUMERIC), 0, DATE '2026-03-10', 'ATIVO', ?)
                """,
                sku,
                descricao,
                saldo,
                valor,
                categoriaId);
    }
}
