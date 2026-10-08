package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.PedidoCompraItem;

import java.math.BigDecimal;

// Uma linha do pedido de compra.
public record CompraItemResponse(
        Long id,
        Long ingredienteId,
        String ingredienteNome,
        String unidade,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
    public static CompraItemResponse de(PedidoCompraItem item) {
        return new CompraItemResponse(
                item.getId(),
                item.getIngrediente().getId(),
                item.getIngrediente().getNome(),
                item.getIngrediente().getUnidadePadrao(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal());
    }
}
