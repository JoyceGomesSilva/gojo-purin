package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Prato;

import java.math.BigDecimal;
import java.util.List;

// Prato na tela de detalhe: os mesmos dados do cardapio mais os complementos.
public record PratoDetalheResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        BigDecimal preco,
        Integer tempoPreparoMin,
        String anime,
        String personagem,
        Long categoriaId,
        String categoriaNome,
        List<GrupoOpcaoResponse> gruposOpcoes
) {
    public static PratoDetalheResponse de(Prato prato, List<GrupoOpcaoResponse> gruposOpcoes) {
        return new PratoDetalheResponse(
                prato.getId(),
                prato.getNome(),
                prato.getDescricao(),
                prato.getFotoUrl(),
                prato.getPrecoVenda(),
                prato.getTempoPreparoMin(),
                prato.getAnime(),
                prato.getPersonagem(),
                prato.getCategoria().getId(),
                prato.getCategoria().getNome(),
                gruposOpcoes);
    }
}
