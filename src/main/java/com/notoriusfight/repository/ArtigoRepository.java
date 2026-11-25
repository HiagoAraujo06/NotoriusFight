package com.notoriusfight.repository;

import com.notoriusfight.domain.Artigo;
import com.notoriusfight.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtigoRepository extends JpaRepository<Artigo, Long> {

    Optional<Artigo> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Artigo> findByCategoriaId(Long categoriaId);

    List<Artigo> findByStatus(Status status);

    @EntityGraph(attributePaths = {"categoria", "fornecedor"})
    @Query("select p from Artigo p order by p.id")
    List<Artigo> buscarTodosComRelacionamentos();

    @EntityGraph(attributePaths = {"categoria", "fornecedor"})
    @Query("select p from Artigo p where p.id = :id")
    Optional<Artigo> buscarPorIdComRelacionamentos(@Param("id") Long id);
}
