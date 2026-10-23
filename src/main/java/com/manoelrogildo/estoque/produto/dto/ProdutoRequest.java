package com.manoelrogildo.estoque.produto.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank @Size(max = 30) String sku,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Pattern(regexp = "\\d{8}", message = "deve conter exatamente 8 dígitos") String ncm,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal precoVenda,
        @NotNull @PositiveOrZero Integer estoqueMinimo) {
}
