package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;

// Um fornecedor na cotacao comparativa (RF-023).
public record CotacaoOfertaResponse(
        Long fornecedorId,
        String razaoSocial,
        String telefone,
        String email,
        BigDecimal preco,
        // true no(s) mais barato(s).
        boolean maisBarato,
        // Quanto % este preco e mais caro que o mais barato (0 para o mais barato).
        BigDecimal diferencaPercentual
) {
}
