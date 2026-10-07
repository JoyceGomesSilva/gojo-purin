package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Categoria;

// Categoria como o painel ve. Diferente da CategoriaResponse do cardapio
// publico, esta leva a ordem e o status.
public record AdminCategoriaResponse(
        Long id,
        String nome,
        String descricao,
        Integer ordem,
        String status
) {
    public static AdminCategoriaResponse de(Categoria categoria) {
        return new AdminCategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getOrdem(),
                categoria.getStatus());
    }
}
