package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Um ingrediente no catalogo do fornecedor (RF-022). Se o ingrediente ja esta
// no catalogo, o preco e atualizado; se nao esta, ele entra.
public record CatalogoItemRequest(
        @NotNull(message = "Escolha o ingrediente")
        Long ingredienteId,

        // Preco de UMA unidade padrao do ingrediente (1 g, 1 ml, 1 un...).
        @NotNull(message = "Informe o preço")
        @DecimalMin(value = "0.0001", message = "O preço deve ser maior que zero")
        @Digits(integer = 6, fraction = 4, message = "O preço deve ter no máximo 4 casas decimais")
        BigDecimal preco
) {
}
