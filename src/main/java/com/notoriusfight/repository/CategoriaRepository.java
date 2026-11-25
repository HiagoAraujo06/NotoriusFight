package com.notoriusfight.repository;

import com.notoriusfight.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoriaRepository extends
        JpaRepository<Categoria, Long>,
        JpaSpecificationExecutor<Categoria> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
