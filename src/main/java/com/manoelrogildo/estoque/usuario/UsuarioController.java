package com.manoelrogildo.estoque.usuario;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.manoelrogildo.estoque.usuario.dto.UsuarioRequest;
import com.manoelrogildo.estoque.usuario.dto.UsuarioResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários (somente ADMIN)")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cadastra um usuário")
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        UsuarioResponse criado = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Lista os usuários")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um usuário (ele não consegue mais fazer login)")
    public ResponseEntity<Void> inativar(@PathVariable Long id, Authentication autenticacao) {
        service.inativar(id, autenticacao.getName());
        return ResponseEntity.noContent().build();
    }
}
