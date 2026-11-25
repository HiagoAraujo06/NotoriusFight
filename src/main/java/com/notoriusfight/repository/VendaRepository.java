package com.notoriusfight.repository;

import com.notoriusfight.domain.Venda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VendaRepository extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {
    @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa", "itens.artigo"})
    Optional<Venda> findOneById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venda v where v.id = :id")
    Optional<Venda> bloquearPorId(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa"})
    Page<Venda> findAll(Specification<Venda> specification, Pageable pageable);
}
