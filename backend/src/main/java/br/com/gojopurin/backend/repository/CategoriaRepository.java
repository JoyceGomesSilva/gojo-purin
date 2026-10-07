package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // SELECT * FROM categoria WHERE status = ? ORDER BY ordem
    List<Categoria> findByStatusOrderByOrdemAsc(String status);

    // Todas (ativas e inativas), para o painel.
    // SELECT * FROM categoria ORDER BY ordem, nome
    List<Categoria> findAllByOrderByOrdemAscNomeAsc();

    // "Ja existe categoria com este nome?" IgnoreCase: "bebida" e "Bebida" contam como iguais.
    boolean existsByNomeIgnoreCase(String nome);

    // A mesma pergunta na edicao, sem contar a propria categoria.
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
