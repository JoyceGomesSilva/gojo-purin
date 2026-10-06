package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.GrupoOpcao;

import java.util.List;

// Um grupo de complementos com suas opcoes ativas.
public record GrupoOpcaoResponse(
        Long id,
        String nome,
        boolean obrigatorio,
        int minEscolhas,
        int maxEscolhas,
        List<OpcaoResponse> opcoes
) {
    public static GrupoOpcaoResponse de(GrupoOpcao grupo) {
        List<OpcaoResponse> opcoes = grupo.getOpcoes().stream()
                .filter(opcao -> "ATIVO".equals(opcao.getStatus()))
                .map(OpcaoResponse::de)
                .toList();
        return new GrupoOpcaoResponse(
                grupo.getId(),
                grupo.getNome(),
                grupo.isObrigatorio(),
                grupo.getMinEscolhas(),
                grupo.getMaxEscolhas(),
                opcoes);
    }
}
