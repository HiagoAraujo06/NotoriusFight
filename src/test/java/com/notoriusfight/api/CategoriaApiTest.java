package com.notoriusfight.api;

import com.notoriusfight.domain.Categoria;
import com.notoriusfight.domain.Artigo;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CategoriaApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository repository;

    @Autowired
    private ArtigoRepository artigoRepository;

    @Test
    void deveAlterarEInativarCategoria() throws Exception {
        Categoria categoria = repository.save(new Categoria("Categoria original"));

        mockMvc.perform(put("/api/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Categoria alterada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Categoria alterada"));

        mockMvc.perform(put("/api/categorias/{id}/status", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INATIVO"));
    }

    @Test
    void deveBuscarCategoriaPorIdParaPreencherTelaDeAlteracao() throws Exception {
        Categoria categoria = repository.save(new Categoria("Categoria para alteração"));

        mockMvc.perform(get("/api/categorias/{id}", categoria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoria.getId()))
                .andExpect(jsonPath("$.nome").value("Categoria para alteração"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    void devePesquisarCategoriasComPaginacao() throws Exception {
        repository.save(new Categoria("Material escolar"));
        repository.save(new Categoria("Equipamentos"));

        mockMvc.perform(get("/api/categorias")
                        .param("nome", "material")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].nome").value("Material escolar"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(5));
    }

    @Test
    void deveExcluirCategoriaSemArtigos() throws Exception {
        Categoria categoria = repository.save(new Categoria("Categoria descartável"));

        mockMvc.perform(delete("/api/categorias/{id}", categoria.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void naoDeveExcluirCategoriaQuePossuiArtigos() throws Exception {
        Categoria categoria = repository.save(new Categoria("Categoria em uso"));
        Artigo artigo = new Artigo(
                "GRUPO-EM-USO-001",
                "Artigo relacionado",
                BigDecimal.ONE,
                BigDecimal.TEN,
                LocalDate.of(2026, 9, 29));
        categoria.adicionarArtigo(artigo);
        artigoRepository.saveAndFlush(artigo);

        mockMvc.perform(delete("/api/categorias/{id}", categoria.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Categoria não pode ser excluída porque possui artigos"));
    }
}
