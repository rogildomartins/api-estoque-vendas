package com.manoelrogildo.estoque.usuario.dto;

import com.manoelrogildo.estoque.usuario.Papel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,60}",
                message = "deve ter de 3 a 60 caracteres: letras, números, ponto, hífen ou sublinhado") String login,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @NotNull Papel papel) {
}
