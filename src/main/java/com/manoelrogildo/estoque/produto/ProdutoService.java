package com.manoelrogildo.estoque.produto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manoelrogildo.estoque.compartilhado.exception.RecursoNaoEncontradoException;
import com.manoelrogildo.estoque.compartilhado.exception.RegraDeNegocioException;
import com.manoelrogildo.estoque.produto.dto.ProdutoRequest;
import com.manoelrogildo.estoque.produto.dto.ProdutoResponse;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest request) {
        String sku = normalizarSku(request.sku());
        if (repository.existsBySku(sku)) {
            throw new RegraDeNegocioException("Já existe um produto com o SKU " + sku);
        }
        Produto produto = new Produto(sku, request.nome().trim(), request.ncm(), request.precoVenda(),
                request.estoqueMinimo());
        return ProdutoResponse.de(repository.save(produto));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listar(String nome, Pageable pageable) {
        return repository.findByAtivoTrueAndNomeContainingIgnoreCase(nome == null ? "" : nome.trim(), pageable)
                .map(ProdutoResponse::de);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse detalhar(Long id) {
        return ProdutoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        if (!produto.getSku().equals(normalizarSku(request.sku()))) {
            throw new RegraDeNegocioException("O SKU de um produto não pode ser alterado");
        }
        produto.atualizar(request.nome().trim(), request.ncm(), request.precoVenda(), request.estoqueMinimo());
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public void inativar(Long id) {
        buscarEntidade(id).inativar();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarComEstoqueBaixo() {
        return repository.buscarComEstoqueBaixo().stream().map(ProdutoResponse::de).toList();
    }

    public Produto buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }

    private String normalizarSku(String sku) {
        return sku.trim().toUpperCase();
    }
}
