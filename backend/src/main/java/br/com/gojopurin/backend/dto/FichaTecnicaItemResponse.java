package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;

// Uma linha da ficha tecnica, ja com o custo calculado.
public record FichaTecnicaItemResponse(
        Long ingredienteId,
        String ingredienteNome,
        String unidade,
        BigDecimal quantidade,
        BigDecimal fatorCorrecao,
        BigDecimal custoUnitario,
        // quantidade x fator de correcao x custo unitario
        BigDecimal custo
) {
}
