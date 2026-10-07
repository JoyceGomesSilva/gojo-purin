package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Uma linha do historico de estoque (RF-033): data, tipo, quantidade,
// motivo, usuario e a referencia (pedido ou compra), quando houver.
public record MovimentacaoResponse(
        Long id,
        LocalDateTime dataHora,
        Long ingredienteId,
        String ingredienteNome,
        String unidade,
        String tipo,
        String motivo,
        BigDecimal quantidade,
        String lote,
        LocalDate validade,
        BigDecimal custoUnitario,
        String usuarioNome,
        Long pedidoId,
        Long pedidoCompraId
) {
}
