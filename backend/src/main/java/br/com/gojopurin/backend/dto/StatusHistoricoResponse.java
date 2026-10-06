package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.PedidoStatusHistorico;

import java.time.LocalDateTime;

// Um ponto da linha do tempo do pedido.
public record StatusHistoricoResponse(
        String status,
        LocalDateTime dataHora
) {
    public static StatusHistoricoResponse de(PedidoStatusHistorico registro) {
        return new StatusHistoricoResponse(registro.getStatus(), registro.getCreatedAt());
    }
}
