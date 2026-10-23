package com.manoelrogildo.estoque.estoque;

import java.time.LocalDateTime;

import com.manoelrogildo.estoque.produto.Produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Registro imutável de cada alteração de saldo (kardex), permitindo auditar
 * a origem de qualquer divergência de estoque.
 */
@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "saldo_anterior", nullable = false)
    private int saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false)
    private int saldoPosterior;

    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    protected MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Produto produto, TipoMovimentacao tipo, int quantidade, int saldoAnterior,
            String motivo) {
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = produto.getQuantidadeEstoque();
        this.motivo = motivo;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getSaldoAnterior() {
        return saldoAnterior;
    }

    public int getSaldoPosterior() {
        return saldoPosterior;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
