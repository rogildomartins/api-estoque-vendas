package com.manoelrogildo.estoque.estoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manoelrogildo.estoque.estoque.dto.MovimentacaoRequest;
import com.manoelrogildo.estoque.estoque.dto.MovimentacaoResponse;
import com.manoelrogildo.estoque.produto.Produto;
import com.manoelrogildo.estoque.produto.ProdutoService;

@Service
public class EstoqueService {

    private final ProdutoService produtoService;
    private final MovimentacaoEstoqueRepository repository;

    public EstoqueService(ProdutoService produtoService, MovimentacaoEstoqueRepository repository) {
        this.produtoService = produtoService;
        this.repository = repository;
    }

    @Transactional
    public MovimentacaoResponse registrar(Long produtoId, MovimentacaoRequest request) {
        Produto produto = produtoService.buscarEntidade(produtoId);
        return MovimentacaoResponse.de(movimentar(produto, request.tipo(), request.quantidade(), request.motivo()));
    }

    /**
     * Aplica a movimentação no saldo do produto e grava o histórico.
     * Deve ser chamado dentro de uma transação (ex.: ao concluir ou cancelar uma venda).
     */
    @Transactional
    public MovimentacaoEstoque movimentar(Produto produto, TipoMovimentacao tipo, int quantidade, String motivo) {
        int saldoAnterior = produto.getQuantidadeEstoque();
        switch (tipo) {
            case ENTRADA -> produto.darEntrada(quantidade);
            case SAIDA -> produto.darSaida(quantidade);
            case AJUSTE -> produto.ajustarPara(quantidade);
        }
        return repository.save(new MovimentacaoEstoque(produto, tipo, quantidade, saldoAnterior, motivo.trim()));
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoResponse> historico(Long produtoId, Pageable pageable) {
        produtoService.buscarEntidade(produtoId);
        return repository.findByProdutoIdOrderByDataHoraDescIdDesc(produtoId, pageable)
                .map(MovimentacaoResponse::de);
    }
}
