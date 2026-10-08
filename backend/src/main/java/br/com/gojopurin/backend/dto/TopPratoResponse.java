package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;

// Uma linha do ranking de pratos mais vendidos (RF-035).
public record TopPratoResponse(
        Long pratoId,
        String nome,
        long quantidade,
        BigDecimal faturamento
) {
}
