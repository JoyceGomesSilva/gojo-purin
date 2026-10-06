package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.PedidoItem;
import br.com.gojopurin.backend.model.PedidoItemOpcao;

import java.math.BigDecimal;
import java.util.List;

public record PedidoItemResponse(
        Long id,
        Long pratoId,
        String pratoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal,
        String observacoes,
        String comboNome,
        String comboChave,
        List<String> opcoes
) {
    public static PedidoItemResponse de(PedidoItem item) {
        return new PedidoItemResponse(
                item.getId(),
                item.getPrato().getId(),
                item.getPrato().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal(),
                item.getObservacoes(),
                item.getComboNome(),
                item.getComboChave(),
                item.getOpcoes().stream().map(PedidoItemOpcao::getNome).toList());
    }
}
