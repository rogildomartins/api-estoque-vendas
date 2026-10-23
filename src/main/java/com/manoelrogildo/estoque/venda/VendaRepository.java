package com.manoelrogildo.estoque.venda;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    /** Carrega itens e produtos em uma única consulta, evitando o problema N+1. */
    @EntityGraph(attributePaths = { "itens", "itens.produto" })
    Optional<Venda> findComItensById(Long id);
}
