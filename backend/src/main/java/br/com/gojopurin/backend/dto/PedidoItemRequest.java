package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Uma linha do carrinho enviada pelo front.
// Repare que NAO vem preco: o back calcula tudo a partir do banco.
public record PedidoItemRequest(
        @NotNull(message = "Informe o prato")
        Long pratoId,

        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade mínima é 1")
        @Max(value = 20, message = "A quantidade máxima por item é 20")
        Integer quantidade,

        @Size(max = 255, message = "Observação muito longa")
        String observacoes,

        // Ids dos complementos marcados (pode vir vazio).
        List<Long> opcaoIds,

        // Preenchidos quando a linha faz parte de um combo.
        Long comboId,

        @Size(max = 40, message = "Identificador de combo inválido")
        String comboChave
) {
}
