package com.manoelrogildo.estoque.venda;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manoelrogildo.estoque.compartilhado.exception.RecursoNaoEncontradoException;
import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;
import com.manoelrogildo.estoque.estoque.EstoqueService;
import com.manoelrogildo.estoque.estoque.TipoMovimentacao;
import com.manoelrogildo.estoque.produto.ProdutoService;
import com.manoelrogildo.estoque.venda.dto.CancelamentoRequest;
import com.manoelrogildo.estoque.venda.dto.VendaRequest;
import com.manoelrogildo.estoque.venda.dto.VendaResponse;
import com.manoelrogildo.estoque.venda.dto.VendaResumoResponse;

@Service
public class VendaService {

    private final VendaRepository repository;
    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;

    public VendaService(VendaRepository repository, ProdutoService produtoService, EstoqueService estoqueService) {
        this.repository = repository;
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
    }

    /**
     * Registra a venda e dá baixa no estoque de todos os itens na mesma transação:
     * se algum item não tiver saldo, nada é gravado.
     */
    @Transactional
    public VendaResponse registrar(VendaRequest request) {
        validarProdutosRepetidos(request);

        Venda venda = new Venda();
        for (VendaRequest.Item item : request.itens()) {
            venda.adicionarItem(produtoService.buscarEntidade(item.produtoId()), item.quantidade());
        }
        repository.save(venda);

        for (ItemVenda item : venda.getItens()) {
            estoqueService.movimentar(item.getProduto(), TipoMovimentacao.SAIDA, item.getQuantidade(),
                    "Venda #" + venda.getId());
        }
        return VendaResponse.de(venda);
    }

    /** Cancela a venda e devolve ao estoque as quantidades vendidas. */
    @Transactional
    public VendaResponse cancelar(Long id, CancelamentoRequest request) {
        Venda venda = buscarComItens(id);
        venda.cancelar(request.motivo().trim());

        for (ItemVenda item : venda.getItens()) {
            estoqueService.movimentar(item.getProduto(), TipoMovimentacao.ENTRADA, item.getQuantidade(),
                    "Cancelamento da venda #" + venda.getId());
        }
        return VendaResponse.de(venda);
    }

    @Transactional(readOnly = true)
    public VendaResponse detalhar(Long id) {
        return VendaResponse.de(buscarComItens(id));
    }

    @Transactional(readOnly = true)
    public Page<VendaResumoResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(VendaResumoResponse::de);
    }

    private Venda buscarComItens(Long id) {
        return repository.findComItensById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Venda", id));
    }

    private void validarProdutosRepetidos(VendaRequest request) {
        Set<Long> vistos = new HashSet<>();
        for (VendaRequest.Item item : request.itens()) {
            if (!vistos.add(item.produtoId())) {
                throw new RegraDeNegocioException(
                        "O produto " + item.produtoId() + " aparece mais de uma vez. Informe a quantidade total em um único item");
            }
        }
    }
}
