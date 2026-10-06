package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Opcao;

import java.math.BigDecimal;

public record OpcaoResponse(
        Long id,
        String nome,
        BigDecimal precoAdicional
) {
    public static OpcaoResponse de(Opcao opcao) {
        return new OpcaoResponse(opcao.getId(), opcao.getNome(), opcao.getPrecoAdicional());
    }
}
