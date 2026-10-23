package com.manoelrogildo.estoque.estoque;

public enum TipoMovimentacao {
    /** Compra, devolução ou cancelamento de venda: soma ao saldo. */
    ENTRADA,
    /** Venda, perda ou consumo interno: subtrai do saldo. */
    SAIDA,
    /** Inventário: define o saldo igual à contagem física. */
    AJUSTE
}
