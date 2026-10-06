package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Pedido como o cliente ve: itens, valores e a linha do tempo.
public record PedidoResponse(
        Long id,
        String status,
        BigDecimal valorTotal,
        String enderecoEntrega,
        String observacoes,
        String motivoCancelamento,
        boolean pago,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        List<PedidoItemResponse> itens,
        List<StatusHistoricoResponse> historico
) {
    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getEnderecoEntrega(),
                pedido.getObservacoes(),
                pedido.getMotivoCancelamento(),
                pedido.isPago(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt(),
                pedido.getItens().stream().map(PedidoItemResponse::de).toList(),
                pedido.getHistorico().stream().map(StatusHistoricoResponse::de).toList());
    }
}
