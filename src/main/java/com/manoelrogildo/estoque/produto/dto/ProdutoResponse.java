package com.manoelrogildo.estoque.produto.dto;

import java.math.BigDecimal;

import com.manoelrogildo.estoque.produto.Produto;

public record ProdutoResponse(
        Long id,
        String sku,
        String nome,
        String ncm,
        BigDecimal precoVenda,
        int quantidadeEstoque,
        int estoqueMinimo,
        boolean estoqueBaixo,
        boolean ativo) {

    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getSku(), p.getNome(), p.getNcm(), p.getPrecoVenda(),
                p.getQuantidadeEstoque(), p.getEstoqueMinimo(), p.isEstoqueBaixo(), p.isAtivo());
    }
}
