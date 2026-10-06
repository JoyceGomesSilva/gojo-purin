package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Pedido como a equipe ve no painel: tudo o que o cliente ve, mais os dados
// do cliente e o canal (RF-020).
public record AdminPedidoResponse(
        Long id,
        String status,
        String canal,
        BigDecimal valorTotal,
        boolean pago,
        String clienteNome,
        String clienteTelefone,
        String clienteEmail,
        String enderecoEntrega,
        String observacoes,
        String motivoCancelamento,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        List<PedidoItemResponse> itens,
        List<StatusHistoricoResponse> historico
) {
    public static AdminPedidoResponse de(Pedido pedido) {
        return new AdminPedidoResponse(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getCanal(),
                pedido.getValorTotal(),
                pedido.isPago(),
                pedido.getCliente().getNome(),
                pedido.getCliente().getTelefone(),
                pedido.getCliente().getEmail(),
                pedido.getEnderecoEntrega(),
                pedido.getObservacoes(),
                pedido.getMotivoCancelamento(),
                pedido.getCreatedAt(),
                pedido.getUpdatedAt(),
                pedido.getItens().stream().map(PedidoItemResponse::de).toList(),
                pedido.getHistorico().stream().map(StatusHistoricoResponse::de).toList());
    }
}
