package br.com.gojopurin.backend.dto;

import jakarta.validation.Valid;

import java.util.List;

// O que o painel envia ao registrar o recebimento (RF-025).
// A lista e opcional: serve so para informar lote e validade de cada item.
public record RecebimentoRequest(
        @Valid
        List<RecebimentoItemRequest> itens
) {
}
