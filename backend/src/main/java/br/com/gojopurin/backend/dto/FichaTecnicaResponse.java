package br.com.gojopurin.backend.dto;

import java.math.BigDecimal;
import java.util.List;

// A ficha tecnica de um prato com todas as contas feitas (RF-011 a RF-013).
public record FichaTecnicaResponse(
        Long pratoId,
        String pratoNome,
        String pratoStatus,
        BigDecimal precoVenda,
        Integer rendimento,
        String modoPreparo,
        List<FichaTecnicaItemResponse> itens,
        // Soma dos custos de todos os ingredientes da receita.
        BigDecimal custoTotal,
        // custoTotal / rendimento: quanto custa UMA porcao.
        BigDecimal custoPorcao,
        // (custoPorcao / precoVenda) x 100. null se a ficha esta vazia.
        BigDecimal foodCost,
        // VERDE, AMARELO ou VERMELHO (RF-013). null se a ficha esta vazia.
        String faixa,
        // RN02: preenchido quando o food cost passa de 35%. So avisa, nao bloqueia.
        String aviso
) {
}
