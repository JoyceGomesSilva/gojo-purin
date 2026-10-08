package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Uma linha do pedido de compra. Nao vem preco: o back usa o preco do
// catalogo do fornecedor.
public record CompraItemRequest(
        @NotNull(message = "Escolha o ingrediente")
        Long ingredienteId,

        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "A quantidade deve ter no máximo 3 casas decimais")
        BigDecimal quantidade
) {
}
