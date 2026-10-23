package com.manoelrogildo.estoque.venda;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.manoelrogildo.estoque.venda.dto.CancelamentoRequest;
import com.manoelrogildo.estoque.venda.dto.VendaRequest;
import com.manoelrogildo.estoque.venda.dto.VendaResponse;
import com.manoelrogildo.estoque.venda.dto.VendaResumoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendas")
@Tag(name = "Vendas", description = "Registro e cancelamento de vendas com baixa automática de estoque")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registra uma venda e dá baixa no estoque dos itens")
    public ResponseEntity<VendaResponse> registrar(@RequestBody @Valid VendaRequest request) {
        VendaResponse venda = service.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(venda.id()).toUri();
        return ResponseEntity.created(location).body(venda);
    }

    @GetMapping
    @Operation(summary = "Lista vendas, da mais recente para a mais antiga")
    public Page<VendaResumoResponse> listar(
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma venda com seus itens")
    public VendaResponse detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping("/{id}/cancelamento")
    @Operation(summary = "Cancela a venda e devolve os itens ao estoque")
    public VendaResponse cancelar(@PathVariable Long id, @RequestBody @Valid CancelamentoRequest request) {
        return service.cancelar(id, request);
    }
}
