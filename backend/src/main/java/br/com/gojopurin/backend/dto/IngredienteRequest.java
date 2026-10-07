package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// O que o painel envia para criar ou editar um ingrediente (RF-027).
public record IngredienteRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "Nome muito longo")
        String nome,

        // Codigo do item no estoque, ex.: ING-056. Unico.
        @NotBlank(message = "Informe o SKU")
        @Size(max = 30, message = "SKU muito longo")
        String sku,

        @NotBlank(message = "Informe a unidade")
        @Pattern(regexp = "G|ML|UN|KG|L", message = "A unidade deve ser G, ML, UN, KG ou L")
        String unidadePadrao,

        // Abaixo desta quantidade o sistema alerta.
        @NotNull(message = "Informe o estoque mínimo")
        @DecimalMin(value = "0", message = "O estoque mínimo não pode ser negativo")
        @Digits(integer = 9, fraction = 3, message = "O estoque mínimo deve ter no máximo 3 casas decimais")
        BigDecimal estoqueMinimo,

        // Custo de UMA unidade padrao (um grama, um ml, uma unidade).
        @NotNull(message = "Informe o custo unitário")
        @DecimalMin(value = "0", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 6, fraction = 4, message = "O custo unitário deve ter no máximo 4 casas decimais")
        BigDecimal custoUnitario,

        @NotBlank(message = "Informe o status")
        @Pattern(regexp = "ATIVO|INATIVO", message = "O status deve ser ATIVO ou INATIVO")
        String status
) {
}
