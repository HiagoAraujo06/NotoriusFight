package com.notoriusfight.api;

import com.notoriusfight.domain.Fornecedor;
import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
import com.notoriusfight.repository.FornecedorRepository;
import com.notoriusfight.repository.CategoriaRepository;
import com.notoriusfight.repository.ArtigoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ArtigoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ArtigoRepository artigoRepository;

    @Test
    void deveCadastrarArtigoERetornar201() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria API"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor API",
                "11222333000181"));

        String json = """
                {
                  "sku": "API-001",
                  "descricao": "Artigo criado pela API",
                  "saldoEstoque": 10.000,
                  "valorUnitario": 49.90,
                  "estoqueMinimo": 2.000,
                  "categoriaId": %d,
                  "fornecedorId": %d
                }
                """.formatted(categoria.getId(), fornecedor.getId());

        mockMvc.perform(post("/api/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sku").value("API-001"))
                .andExpect(jsonPath("$.categoriaNome").value("Categoria API"))
                .andExpect(jsonPath("$.fornecedorRazaoSocial").value("Fornecedor API"));
    }

    @Test
    void deveRetornar400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/api/artigos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Um ou mais campos são inválidos"))
                .andExpect(jsonPath("$.fields.sku").exists())
                .andExpect(jsonPath("$.fields.categoriaId").exists());
    }

    @Test
    void deveRetornar404ParaArtigoInexistente() throws Exception {
        mockMvc.perform(get("/api/artigos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Artigo não encontrado"))
                .andExpect(jsonPath("$.path").value("/api/artigos/" + Long.MAX_VALUE));
    }

    @Test
    void deveBuscarArtigoPorIdParaPreencherTelaDeAlteracao() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria formulário"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor formulário",
                "11444777000161"));
        Artigo artigo = new Artigo(
                "API-FORMULARIO-001",
                "Artigo para alteração",
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
        categoria.adicionarArtigo(artigo);
        artigo.associarFornecedor(fornecedor);
        artigoRepository.save(artigo);

        mockMvc.perform(get("/api/artigos/{id}", artigo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artigo.getId()))
                .andExpect(jsonPath("$.sku").value("API-FORMULARIO-001"))
                .andExpect(jsonPath("$.descricao").value("Artigo para alteração"))
                .andExpect(jsonPath("$.saldoEstoque").value(10.0))
                .andExpect(jsonPath("$.valorUnitario").value(49.9))
                .andExpect(jsonPath("$.estoqueMinimo").value(2.0))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.categoriaId").value(categoria.getId()))
                .andExpect(jsonPath("$.categoriaNome").value("Categoria formulário"))
                .andExpect(jsonPath("$.fornecedorId").value(fornecedor.getId()))
                .andExpect(jsonPath("$.fornecedorRazaoSocial")
                        .value("Fornecedor formulário"));
    }

    @Test
    void deveAlterarStatusEMovimentarEstoque() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria operações"));
        Artigo artigo = salvarArtigo(categoria, "API-OPERACOES-001", "Artigo original");

        String alteracao = """
                {
                  "descricao": "Artigo alterado",
                  "valorUnitario": 59.90,
                  "estoqueMinimo": 4.000,
                  "categoriaId": %d,
                  "fornecedorId": null
                }
                """.formatted(categoria.getId());

        mockMvc.perform(put("/api/artigos/{id}", artigo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Artigo alterado"));

        mockMvc.perform(put("/api/artigos/{id}/status", artigo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));

        mockMvc.perform(post("/api/artigos/{id}/estoque/entradas", artigo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":3.000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(13.0));

        mockMvc.perform(post("/api/artigos/{id}/estoque/saidas", artigo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":2.000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEstoque").value(11.0));
    }

    @Test
    void devePesquisarArtigosComFiltrosEPaginacao() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria pesquisa"));
        salvarArtigo(categoria, "API-PESQUISA-001", "Mouthguard ergonômico");
        salvarArtigo(categoria, "API-PESQUISA-002", "Caneleira anatômica");

        mockMvc.perform(get("/api/artigos")
                        .param("descricao", "mouthguard")
                        .param("categoriaId", categoria.getId().toString())
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "descricao,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].descricao").value("Mouthguard ergonômico"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deveExcluirArtigo() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria exclusão"));
        Artigo artigo = salvarArtigo(categoria, "API-EXCLUSAO-001", "Artigo descartável");

        mockMvc.perform(delete("/api/artigos/{id}", artigo.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueOSaldo() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria saldo insuficiente"));
        Artigo artigo = salvarArtigo(
                categoria,
                "API-SALDO-INSUFICIENTE-001",
                "Artigo com saldo limitado");

        mockMvc.perform(post("/api/artigos/{id}/estoque/saidas", artigo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":10.001}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo de estoque insuficiente"));
    }

    @Test
    void deveRejeitarCampoDeOrdenacaoNaoPermitido() throws Exception {
        mockMvc.perform(get("/api/artigos")
                        .param("sort", "fornecedor.razaoSocial,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Campo de ordenação inválido: fornecedor.razaoSocial"));
    }

    private Artigo salvarArtigo(Categoria categoria, String codigo, String descricao) {
        Artigo artigo = new Artigo(
                codigo,
                descricao,
                new BigDecimal("10.000"),
                new BigDecimal("49.90"),
                new BigDecimal("2.000"),
                LocalDate.of(2026, 9, 29));
        categoria.adicionarArtigo(artigo);
        return artigoRepository.save(artigo);
    }
}
