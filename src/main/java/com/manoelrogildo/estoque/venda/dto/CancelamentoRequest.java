package com.manoelrogildo.estoque.venda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoRequest(@NotBlank @Size(max = 200) String motivo) {
}
