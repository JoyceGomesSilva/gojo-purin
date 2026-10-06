package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Pedido;

import java.time.LocalDateTime;
import java.util.List;

// Resposta leve de GET /api/pedidos/{id}/status: so o necessario para a tela
// de acompanhamento se atualizar de tempos em tempos.
public record PedidoStatusResponse(
        Long id,
        String status,
        String motivoCancelamento,
        LocalDateTime atualizadoEm,
        List<StatusHistoricoResponse> historico
) {
    public static PedidoStatusResponse de(Pedido pedido) {
        return new PedidoStatusResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getMotivoCancelamento(),
                pedido.getUpdatedAt(),
                pedido.getHistorico().stream().map(StatusHistoricoResponse::de).toList());
    }
}
