package com.manoelrogildo.estoque.compartilhado.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio
 * (ex.: estoque insuficiente, SKU duplicado, venda já cancelada).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
