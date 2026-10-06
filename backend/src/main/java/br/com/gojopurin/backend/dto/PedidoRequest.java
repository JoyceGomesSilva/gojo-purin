package br.com.gojopurin.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

// O que o front envia para criar um pedido (o carrinho + o endereco).
public record PedidoRequest(
        @NotBlank(message = "Informe o endereço de entrega")
        @Size(max = 255, message = "Endereço muito longo")
        String enderecoEntrega,

        @Size(max = 500, message = "Observação muito longa")
        String observacoes,

        @NotEmpty(message = "O carrinho está vazio")
        @Size(max = 60, message = "Pedido com itens demais")
        @Valid
        List<PedidoItemRequest> itens
) {
}
