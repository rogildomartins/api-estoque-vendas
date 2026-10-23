package com.manoelrogildo.estoque.venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;
import com.manoelrogildo.estoque.produto.Produto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusVenda status;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    public Venda() {
        this.dataHora = LocalDateTime.now();
        this.status = StatusVenda.CONCLUIDA;
    }

    public void adicionarItem(Produto produto, int quantidade) {
        produto.validarDisponivelParaVenda();
        ItemVenda item = new ItemVenda(this, produto, quantidade);
        itens.add(item);
        valorTotal = valorTotal.add(item.getSubtotal());
    }

    public void cancelar(String motivo) {
        if (status == StatusVenda.CANCELADA) {
            throw new RegraDeNegocioException("A venda #" + id + " já está cancelada");
        }
        this.status = StatusVenda.CANCELADA;
        this.canceladaEm = LocalDateTime.now();
        this.motivoCancelamento = motivo;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public StatusVenda getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getCanceladaEm() {
        return canceladaEm;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public List<ItemVenda> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
