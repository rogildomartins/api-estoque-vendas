package com.manoelrogildo.estoque.usuario.dto;

import com.manoelrogildo.estoque.usuario.Papel;
import com.manoelrogildo.estoque.usuario.Usuario;

public record UsuarioResponse(Long id, String login, String nome, Papel papel, boolean ativo) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getNome(), u.getPapel(), u.isAtivo());
    }
}
