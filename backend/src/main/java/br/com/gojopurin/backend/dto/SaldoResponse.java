package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Ingrediente;

import java.math.BigDecimal;

// O saldo atual de um ingrediente (RF-031) e se ele esta abaixo do minimo (RF-032).
public record SaldoResponse(
        Long ingredienteId,
        String nome,
        String sku,
        String unidade,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        BigDecimal custoUnitario,
        boolean abaixoDoMinimo
) {
    public static SaldoResponse de(Ingrediente ingrediente, BigDecimal saldo) {
        return new SaldoResponse(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getSku(),
                ingrediente.getUnidadePadrao(),
                saldo,
                ingrediente.getEstoqueMinimo(),
                ingrediente.getCustoUnitario(),
                // compareTo < 0 significa "saldo menor que o minimo".
                saldo.compareTo(ingrediente.getEstoqueMinimo()) < 0);
    }
}
