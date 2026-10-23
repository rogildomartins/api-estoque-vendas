package com.manoelrogildo.estoque.produto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsBySku(String sku);

    Page<Produto> findByAtivoTrueAndNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("""
            select p from Produto p
            where p.ativo = true and p.quantidadeEstoque <= p.estoqueMinimo
            order by p.quantidadeEstoque
            """)
    List<Produto> buscarComEstoqueBaixo();
}
