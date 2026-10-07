package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;

// Resposta de GET /api/admin/pratos/{id}/custo: so os numeros do custo,
// sem a lista de ingredientes.
public record CustoResponse(
        Long pratoId,
        String pratoNome,
        BigDecimal precoVenda,
        Integer rendimento,
        BigDecimal custoTotal,
        BigDecimal custoPorcao,
        BigDecimal foodCost,
        String faixa,
        String aviso
) {
    public static CustoResponse de(FichaTecnicaResponse ficha) {
        return new CustoResponse(
                ficha.pratoId(),
                ficha.pratoNome(),
                ficha.precoVenda(),
                ficha.rendimento(),
                ficha.custoTotal(),
                ficha.custoPorcao(),
                ficha.foodCost(),
                ficha.faixa(),
                ficha.aviso());
    }
}
