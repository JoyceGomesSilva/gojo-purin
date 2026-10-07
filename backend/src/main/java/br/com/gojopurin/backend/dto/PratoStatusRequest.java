package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// O que o painel envia para trocar so o status de um prato.
public record PratoStatusRequest(
        @NotBlank(message = "Informe o status")
        @Pattern(regexp = "ATIVO|INATIVO|PAUSADO", message = "O status deve ser ATIVO, INATIVO ou PAUSADO")
        String status
) {
}
