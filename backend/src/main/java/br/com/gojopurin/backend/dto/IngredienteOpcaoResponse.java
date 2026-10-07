package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Ingrediente;

import java.math.BigDecimal;

// Ingrediente resumido, para o campo de escolha da ficha tecnica.
public record IngredienteOpcaoResponse(
        Long id,
        String nome,
        String unidadePadrao,
        BigDecimal custoUnitario,
        String status
) {
    public static IngredienteOpcaoResponse de(Ingrediente ingrediente) {
        return new IngredienteOpcaoResponse(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getUnidadePadrao(),
                ingrediente.getCustoUnitario(),
                ingrediente.getStatus());
    }
}
