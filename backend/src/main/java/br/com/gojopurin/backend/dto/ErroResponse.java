package br.com.gojopurin.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

// Formato padrao de todas as respostas de erro da API (RNF06).
public record ErroResponse(
        int status,
        String mensagem,
        List<String> detalhes,
        LocalDateTime dataHora
) {
}
