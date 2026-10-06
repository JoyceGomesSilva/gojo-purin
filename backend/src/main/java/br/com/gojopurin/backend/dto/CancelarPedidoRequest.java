package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Corpo de PATCH /api/admin/pedidos/{id}/cancelar. O motivo e obrigatorio (RF-018).
public record CancelarPedidoRequest(
        @NotBlank(message = "Informe o motivo do cancelamento")
        @Size(max = 255, message = "Motivo muito longo")
        String motivo
) {
}
