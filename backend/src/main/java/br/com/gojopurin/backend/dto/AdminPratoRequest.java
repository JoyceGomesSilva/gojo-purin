package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// O que o painel envia para criar ou editar um prato (RF-010).
// O modo de preparo (RF-014) e preenchido na tela da ficha tecnica.
public record AdminPratoRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "Nome muito longo")
        String nome,

        @Size(max = 500, message = "Descrição muito longa")
        String descricao,

        // Opcional. Se vier, precisa ser um endereco de internet.
        @Size(max = 500, message = "Endereço da foto muito longo")
        @Pattern(regexp = "^(https?://.+)?$", message = "A foto deve ser um endereço começando com http:// ou https://")
        String fotoUrl,

        // Digits: ate 8 digitos antes da virgula e 2 depois, como a coluna NUMERIC(10,2).
        @NotNull(message = "Informe o preço")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
        BigDecimal precoVenda,

        @NotNull(message = "Informe o tempo de preparo")
        @Min(value = 1, message = "O tempo de preparo deve ser de pelo menos 1 minuto")
        @Max(value = 600, message = "O tempo de preparo deve ser de no máximo 600 minutos")
        Integer tempoPreparoMin,

        @NotNull(message = "Informe a categoria")
        Long categoriaId,

        @NotBlank(message = "Informe o status")
        @Pattern(regexp = "ATIVO|INATIVO|PAUSADO", message = "O status deve ser ATIVO, INATIVO ou PAUSADO")
        String status,

        // Extras do tema do Gojo Purin.
        @Size(max = 80, message = "Nome do anime muito longo")
        String anime,

        @Size(max = 80, message = "Nome do personagem muito longo")
        String personagem
) {
}
