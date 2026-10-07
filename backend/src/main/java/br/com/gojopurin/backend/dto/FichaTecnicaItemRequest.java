package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Uma linha da ficha tecnica enviada pelo painel (RF-011).
// A unidade nao vem aqui: e sempre a unidade padrao do ingrediente (g, ml ou un),
// a mesma em que o estoque e o custo unitario sao guardados.
public record FichaTecnicaItemRequest(
        @NotNull(message = "Escolha o ingrediente")
        Long ingredienteId,

        // O que vai no prato, na unidade do ingrediente.
        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "A quantidade deve ter no máximo 3 casas decimais")
        BigDecimal quantidade,

        // RN08: fator de correcao nunca menor que 1.0.
        @NotNull(message = "Informe o fator de correção")
        @Min(value = 1, message = "O fator de correção não pode ser menor que 1,0")
        @DecimalMax(value = "9.99", message = "O fator de correção deve ser no máximo 9,99")
        @Digits(integer = 1, fraction = 2, message = "O fator de correção deve ter no máximo 2 casas decimais")
        BigDecimal fatorCorrecao
) {
}
