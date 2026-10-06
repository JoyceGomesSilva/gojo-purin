package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.NotBlank;

// Corpo de PATCH /api/admin/pedidos/{id}/status
public record MudarStatusRequest(
        @NotBlank(message = "Informe o novo status")
        String status
) {
}
