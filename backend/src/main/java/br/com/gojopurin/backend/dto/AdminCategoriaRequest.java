package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O que o painel envia para criar ou editar uma categoria (RF-009).
public record AdminCategoriaRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 100, message = "Nome muito longo")
        String nome,

        @Size(max = 255, message = "Descrição muito longa")
        String descricao,

        // Posicao no cardapio: 1 aparece primeiro.
        @NotNull(message = "Informe a ordem")
        @Min(value = 0, message = "A ordem não pode ser negativa")
        @Max(value = 999, message = "A ordem deve ser no máximo 999")
        Integer ordem,

        @NotBlank(message = "Informe o status")
        @Pattern(regexp = "ATIVO|INATIVO", message = "O status deve ser ATIVO ou INATIVO")
        String status
) {
}
