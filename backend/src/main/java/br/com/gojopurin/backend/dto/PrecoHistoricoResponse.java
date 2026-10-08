package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Um ponto do historico de precos (RF-026).
public record PrecoHistoricoResponse(
        Long fornecedorId,
        String razaoSocial,
        BigDecimal preco,
        LocalDateTime dataHora
) {
}
