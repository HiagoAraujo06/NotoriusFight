package com.notoriusfight.api;

import com.notoriusfight.domain.Fornecedor;
import com.notoriusfight.domain.Categoria;
import com.notoriusfight.repository.FornecedorRepository;
import com.notoriusfight.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArtigoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Test
    void deveCadastrarArtigoERetornar201() throws Exception {
        Categoria categoria = categoriaRepository.save(new Categoria("Categoria API"));
        Fornecedor fornecedor = fornecedorRepository.save(new Fornecedor(
                "Fornecedor API",
                "22222222000192"));

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
}
