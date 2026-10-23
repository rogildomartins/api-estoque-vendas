package com.manoelrogildo.estoque.produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nome;

    /** Nomenclatura Comum do Mercosul, obrigatória na emissão de NF-e/NFC-e. */
    @Column(nullable = false, length = 8)
    private String ncm;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEstoque;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    /** Controle de concorrência otimista: evita que duas vendas simultâneas corrompam o saldo. */
    @Version
    private Long versao;

    protected Produto() {
    }

    public Produto(String sku, String nome, String ncm, BigDecimal precoVenda, int estoqueMinimo) {
        this.sku = sku;
        this.nome = nome;
        this.ncm = ncm;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
    }

    public void atualizar(String nome, String ncm, BigDecimal precoVenda, int estoqueMinimo) {
        this.nome = nome;
        this.ncm = ncm;
        this.precoVenda = precoVenda;
        this.estoqueMinimo = estoqueMinimo;
    }

    public void darEntrada(int quantidade) {
        validarQuantidadePositiva(quantidade);
        this.quantidadeEstoque += quantidade;
    }

    public void darSaida(int quantidade) {
        validarQuantidadePositiva(quantidade);
        if (quantidade > quantidadeEstoque) {
            throw new RegraDeNegocioException(
                    "Estoque insuficiente para o produto \"%s\". Disponível: %d, solicitado: %d"
                            .formatted(nome, quantidadeEstoque, quantidade));
        }
        this.quantidadeEstoque -= quantidade;
    }

    /** Ajuste de inventário: define o saldo para a quantidade contada fisicamente. */
    public void ajustarPara(int quantidadeContada) {
        if (quantidadeContada < 0) {
            throw new RegraDeNegocioException("A quantidade de inventário não pode ser negativa");
        }
        this.quantidadeEstoque = quantidadeContada;
    }

    public boolean isEstoqueBaixo() {
        return quantidadeEstoque <= estoqueMinimo;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void validarDisponivelParaVenda() {
        if (!ativo) {
            throw new RegraDeNegocioException(
                    "O produto \"%s\" está inativo e não pode ser vendido".formatted(nome));
        }
    }

    private void validarQuantidadePositiva(int quantidade) {
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
        }
    }

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public String getNcm() {
        return ncm;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
