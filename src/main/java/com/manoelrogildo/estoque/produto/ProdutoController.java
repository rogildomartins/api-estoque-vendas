package com.manoelrogildo.estoque.produto;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.manoelrogildo.estoque.produto.dto.ProdutoRequest;
import com.manoelrogildo.estoque.produto.dto.ProdutoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Cadastro e consulta de produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo produto (estoque inicial zero)")
    public ResponseEntity<ProdutoResponse> cadastrar(@RequestBody @Valid ProdutoRequest request) {
        ProdutoResponse criado = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista produtos ativos, com filtro opcional por nome e paginação")
    public Page<ProdutoResponse> listar(@RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(nome, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um produto")
    public ProdutoResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza dados cadastrais (o saldo só muda por movimentação de estoque)")
    public ProdutoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um produto (exclusão lógica, preserva o histórico)")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Lista produtos com saldo menor ou igual ao estoque mínimo")
    public List<ProdutoResponse> estoqueBaixo() {
        return service.listarComEstoqueBaixo();
    }
}
