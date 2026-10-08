package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.PedidoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Pedido de compra como o painel ve (RF-024).
public record CompraResponse(
        Long id,
        Long fornecedorId,
        String fornecedorNome,
        String status,
        BigDecimal valorTotal,
        LocalDateTime criadoEm,
        List<CompraItemResponse> itens
) {
    public static CompraResponse de(PedidoCompra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getFornecedor().getId(),
                compra.getFornecedor().getRazaoSocial(),
                compra.getStatus(),
                compra.getValorTotal(),
                compra.getCreatedAt(),
                compra.getItens().stream()
                        // Ordem alfabetica de ingrediente, para a tela nao "embaralhar".
                        .sorted((a, b) -> a.getIngrediente().getNome().compareTo(b.getIngrediente().getNome()))
                        .map(CompraItemResponse::de)
                        .toList());
    }
}
