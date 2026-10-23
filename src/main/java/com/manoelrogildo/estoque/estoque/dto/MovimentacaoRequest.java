package com.manoelrogildo.estoque.estoque.dto;

import com.manoelrogildo.estoque.estoque.TipoMovimentacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Para ENTRADA e SAIDA, {@code quantidade} é o quanto movimentar.
 * Para AJUSTE, é a quantidade contada no inventário (novo saldo).
 */
public record MovimentacaoRequest(
        @NotNull TipoMovimentacao tipo,
        @NotNull @PositiveOrZero Integer quantidade,
        @NotBlank @Size(max = 200) String motivo) {
}
