package com.manoelrogildo.estoque.estoque.dto;

import java.time.LocalDateTime;

import com.manoelrogildo.estoque.estoque.MovimentacaoEstoque;
import com.manoelrogildo.estoque.estoque.TipoMovimentacao;

public record MovimentacaoResponse(
        Long id,
        Long produtoId,
        TipoMovimentacao tipo,
        int quantidade,
        int saldoAnterior,
        int saldoPosterior,
        String motivo,
        LocalDateTime dataHora) {

    public static MovimentacaoResponse de(MovimentacaoEstoque m) {
        return new MovimentacaoResponse(m.getId(), m.getProduto().getId(), m.getTipo(), m.getQuantidade(),
                m.getSaldoAnterior(), m.getSaldoPosterior(), m.getMotivo(), m.getDataHora());
    }
}
