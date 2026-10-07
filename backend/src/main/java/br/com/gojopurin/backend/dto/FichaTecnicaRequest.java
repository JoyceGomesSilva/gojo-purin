package br.com.gojopurin.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// A ficha tecnica inteira, como o painel envia para salvar ou simular (RF-011).
public record FichaTecnicaRequest(
        // Quantas porcoes a receita rende.
        @NotNull(message = "Informe o rendimento")
        @Min(value = 1, message = "O rendimento deve ser de pelo menos 1 porção")
        @Max(value = 1000, message = "O rendimento deve ser de no máximo 1000 porções")
        Integer rendimento,

        // RF-014: instrucoes passo a passo. Obrigatorio para SALVAR; a checagem
        // fica no service, porque a simulacao aceita o texto ainda em branco.
        @Size(max = 5000, message = "Modo de preparo muito longo")
        String modoPreparo,

        // @Valid manda validar tambem cada item da lista.
        @NotNull(message = "Envie a lista de ingredientes")
        @Valid
        List<FichaTecnicaItemRequest> itens
) {
}
