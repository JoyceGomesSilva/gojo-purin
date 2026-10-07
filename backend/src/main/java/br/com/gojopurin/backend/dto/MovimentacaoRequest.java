package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Uma movimentacao manual de estoque enviada pelo painel:
// - ENTRADA (RF-028): ajuste, com lote, validade e custo opcionais;
// - SAIDA (RF-030): perda, com motivo obrigatorio.
public record MovimentacaoRequest(
        @NotNull(message = "Escolha o ingrediente")
        Long ingredienteId,

        @NotBlank(message = "Informe o tipo")
        @Pattern(regexp = "ENTRADA|SAIDA", message = "O tipo deve ser ENTRADA ou SAIDA")
        String tipo,

        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
        @Digits(integer = 9, fraction = 3, message = "A quantidade deve ter no máximo 3 casas decimais")
        BigDecimal quantidade,

        // So para SAIDA: DESPERDICIO, VENCIMENTO, QUEBRA ou USO_INTERNO.
        // A conferencia e feita no service, com o enum MotivoPerda.
        String motivo,

        // Os tres abaixo so valem para ENTRADA.
        @Size(max = 50, message = "Lote muito longo")
        String lote,

        // Vem como texto "2026-12-31" e o Spring converte para data.
        LocalDate validade,

        @DecimalMin(value = "0", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 6, fraction = 4, message = "O custo unitário deve ter no máximo 4 casas decimais")
        BigDecimal custoUnitario
) {
}
