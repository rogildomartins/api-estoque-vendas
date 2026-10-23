package com.manoelrogildo.estoque.venda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.manoelrogildo.estoque.venda.ItemVenda;
import com.manoelrogildo.estoque.venda.StatusVenda;
import com.manoelrogildo.estoque.venda.Venda;

public record VendaResponse(
        Long id,
        LocalDateTime dataHora,
        StatusVenda status,
        BigDecimal valorTotal,
        LocalDateTime canceladaEm,
        String motivoCancelamento,
        List<Item> itens) {

    public record Item(Long produtoId, String sku, String nome, int quantidade, BigDecimal precoUnitario,
            BigDecimal subtotal) {

        static Item de(ItemVenda i) {
            return new Item(i.getProduto().getId(), i.getProduto().getSku(), i.getProduto().getNome(),
                    i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal());
        }
    }

    public static VendaResponse de(Venda v) {
        return new VendaResponse(v.getId(), v.getDataHora(), v.getStatus(), v.getValorTotal(), v.getCanceladaEm(),
                v.getMotivoCancelamento(), v.getItens().stream().map(Item::de).toList());
    }
}
