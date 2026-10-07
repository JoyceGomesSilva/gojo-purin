package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Ingrediente;

import java.math.BigDecimal;

// Ingrediente completo, como o painel ve no CRUD (RF-027).
public record IngredienteResponse(
        Long id,
        String nome,
        String sku,
        String unidadePadrao,
        BigDecimal estoqueMinimo,
        BigDecimal custoUnitario,
        String status
) {
    public static IngredienteResponse de(Ingrediente ingrediente) {
        return new IngredienteResponse(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getSku(),
                ingrediente.getUnidadePadrao(),
                ingrediente.getEstoqueMinimo(),
                ingrediente.getCustoUnitario(),
                ingrediente.getStatus());
    }
}
