package br.com.gojopurin.backend.dto;

import java.util.List;

// Uma etapa do combo com os pratos que podem ser escolhidos nela.
public record ComboEtapaResponse(
        Long id,
        String nome,
        boolean obrigatoria,
        List<PratoCardapioResponse> pratos
) {
}
