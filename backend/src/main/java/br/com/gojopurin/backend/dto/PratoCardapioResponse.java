package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Prato;

import java.math.BigDecimal;

// Prato como aparece para o cliente. Nao leva custo nem status: so o que o
// cardapio precisa mostrar (RNF05: nunca devolver a Entity direto).
public record PratoCardapioResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        BigDecimal preco,
        Integer tempoPreparoMin,
        String anime,
        String personagem,
        Long categoriaId,
        String categoriaNome
) {
    public static PratoCardapioResponse de(Prato prato) {
        return new PratoCardapioResponse(
                prato.getId(),
                prato.getNome(),
                prato.getDescricao(),
                prato.getFotoUrl(),
                prato.getPrecoVenda(),
                prato.getTempoPreparoMin(),
                prato.getAnime(),
                prato.getPersonagem(),
                prato.getCategoria().getId(),
                prato.getCategoria().getNome());
    }
}
