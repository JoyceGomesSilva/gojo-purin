package br.com.gojopurin.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

// Formato padrao de todas as listagens paginadas da API (RNF08).
// O <T> significa "de qualquer tipo": serve para pratos, pedidos, ingredientes...
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean ultima
) {
    // Converte a Page do Spring, transformando cada item (Entity) em DTO.
    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> conversor) {
        return new PaginaResponse<>(
                page.getContent().stream().map(conversor).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
