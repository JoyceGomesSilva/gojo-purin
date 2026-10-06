package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Categoria;

// Categoria como aparece no cardapio publico.
public record CategoriaResponse(
        Long id,
        String nome,
        String descricao
) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getDescricao());
    }
}
