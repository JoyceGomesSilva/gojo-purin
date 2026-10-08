package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.util.List;

// Cotacao comparativa de um ingrediente (RF-023) com o historico de precos
// dos fornecedores dele (RF-026), para o grafico de evolucao.
public record CotacaoResponse(
        Long ingredienteId,
        String ingredienteNome,
        String unidade,
        BigDecimal custoAtual,
        // Do mais barato para o mais caro.
        List<CotacaoOfertaResponse> ofertas,
        // Do mais antigo para o mais novo.
        List<PrecoHistoricoResponse> historico
) {
}
