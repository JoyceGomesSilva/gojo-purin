package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// As vendas de um dia, para o grafico e a tabela de vendas por periodo (RF-037).
public record VendaDiaResponse(
        LocalDate dia,
        long pedidos,
        BigDecimal faturamento,
        BigDecimal ticketMedio
) {
}
