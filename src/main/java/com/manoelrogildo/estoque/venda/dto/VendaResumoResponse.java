package com.manoelrogildo.estoque.venda.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.manoelrogildo.estoque.venda.StatusVenda;
import com.manoelrogildo.estoque.venda.Venda;

public record VendaResumoResponse(Long id, LocalDateTime dataHora, StatusVenda status, BigDecimal valorTotal) {

    public static VendaResumoResponse de(Venda v) {
        return new VendaResumoResponse(v.getId(), v.getDataHora(), v.getStatus(), v.getValorTotal());
    }
}
