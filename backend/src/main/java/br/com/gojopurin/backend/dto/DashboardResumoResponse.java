package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Os cards do dashboard (RF-034), sempre do dia de hoje.
public record DashboardResumoResponse(
        LocalDate dia,
        BigDecimal faturamento,
        long pedidos,
        // faturamento / pedidos
        BigDecimal ticketMedio,
        // custo dos pratos vendidos hoje / faturamento de hoje x 100.
        // null quando ainda nao houve venda no dia.
        BigDecimal foodCost,
        // VERDE, AMARELO ou VERMELHO, com as mesmas faixas da ficha tecnica.
        String faixa,
        // Quantos ingredientes estao abaixo do minimo (RF-036).
        int alertasEstoque
) {
}
