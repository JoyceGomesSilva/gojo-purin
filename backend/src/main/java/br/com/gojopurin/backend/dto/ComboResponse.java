package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.util.List;

// Combo como aparece para o cliente.
// precoCheio e precoComDesconto so vem preenchidos nos combos PRONTOS;
// no MONTAVEL o preco depende do que o cliente escolher.
public record ComboResponse(
        Long id,
        String nome,
        String descricao,
        String tipo,
        BigDecimal descontoPercentual,
        String fotoUrl,
        BigDecimal precoCheio,
        BigDecimal precoComDesconto,
        List<ComboEtapaResponse> etapas
) {
}
