package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Lote e validade de um item na hora do recebimento (RF-028). Os dois sao opcionais.
public record RecebimentoItemRequest(
        @NotNull(message = "Informe o item")
        Long itemId,

        @Size(max = 50, message = "Lote muito longo")
        String lote,

        LocalDate validade
) {
}
