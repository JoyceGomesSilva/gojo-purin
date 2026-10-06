package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // SELECT * FROM categoria WHERE status = ? ORDER BY ordem
    List<Categoria> findByStatusOrderByOrdemAsc(String status);
}
