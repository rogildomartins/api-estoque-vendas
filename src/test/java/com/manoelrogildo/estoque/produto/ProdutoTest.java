package com.manoelrogildo.estoque.produto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;

class ProdutoTest {

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("CAF-500G", "Café 500g", "09012100", new BigDecimal("18.90"), 5);
        produto.darEntrada(10);
    }

    @Test
    void darSaidaDeveSubtrairDoSaldo() {
        produto.darSaida(4);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(6);
    }

    @Test
    void darSaidaMaiorQueSaldoDeveFalharSemAlterarEstoque() {
        assertThatThrownBy(() -> produto.darSaida(11))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(10);
    }

    @Test
    void quantidadeZeroOuNegativaDeveSerRejeitada() {
        assertThatThrownBy(() -> produto.darEntrada(0)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> produto.darSaida(-1)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void ajusteDeInventarioDefineNovoSaldo() {
        produto.ajustarPara(3);

        assertThat(produto.getQuantidadeEstoque()).isEqualTo(3);
        assertThat(produto.isEstoqueBaixo()).isTrue();
    }

    @Test
    void ajusteNegativoDeveSerRejeitado() {
        assertThatThrownBy(() -> produto.ajustarPara(-1)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void produtoInativoNaoPodeSerVendido() {
        produto.inativar();

        assertThatThrownBy(produto::validarDisponivelParaVenda)
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("inativo");
    }
}
