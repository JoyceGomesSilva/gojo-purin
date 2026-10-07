package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.FichaTecnica;
import br.com.gojopurin.backend.model.Prato;

import java.math.BigDecimal;

// Prato como o painel ve: os dados do cardapio mais o status, o modo de
// preparo e se ja existe ficha tecnica com ingredientes.
public record AdminPratoResponse(
        Long id,
        String nome,
        String descricao,
        String fotoUrl,
        BigDecimal precoVenda,
        Integer tempoPreparoMin,
        String anime,
        String personagem,
        Long categoriaId,
        String categoriaNome,
        String status,
        String modoPreparo,
        // true = tem ficha com pelo menos 1 ingrediente, entao pode ficar ATIVO (RN01).
        boolean temFicha
) {
    // A ficha pode ser null: prato novo ainda nao tem.
    public static AdminPratoResponse de(Prato prato, FichaTecnica ficha) {
        return new AdminPratoResponse(
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
                prato.getStatus(),
                ficha == null ? null : ficha.getModoPreparo(),
                ficha != null && !ficha.getItens().isEmpty());
    }
}
