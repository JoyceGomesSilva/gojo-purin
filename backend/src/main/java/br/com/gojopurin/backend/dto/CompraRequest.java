package br.com.gojopurin.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// O que o painel envia para criar ou editar um pedido de compra (RF-024).
public record CompraRequest(
        @NotNull(message = "Escolha o fornecedor")
        Long fornecedorId,

        // @NotEmpty: a lista precisa ter pelo menos um item.
        @NotEmpty(message = "Adicione pelo menos um ingrediente")
        @Valid
        List<CompraItemRequest> itens
) {
}
