package com.manoelrogildo.estoque.estoque;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.manoelrogildo.estoque.estoque.dto.MovimentacaoRequest;
import com.manoelrogildo.estoque.estoque.dto.MovimentacaoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos/{produtoId}/movimentacoes")
@Tag(name = "Estoque", description = "Entradas, saídas, ajustes de inventário e histórico (kardex)")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma entrada, saída ou ajuste de inventário")
    public MovimentacaoResponse registrar(@PathVariable Long produtoId,
            @RequestBody @Valid MovimentacaoRequest request) {
        return service.registrar(produtoId, request);
    }

    @GetMapping
    @Operation(summary = "Histórico de movimentações do produto, da mais recente para a mais antiga")
    public Page<MovimentacaoResponse> historico(@PathVariable Long produtoId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.historico(produtoId, pageable);
    }
}
