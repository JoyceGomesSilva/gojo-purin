package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    // Todos os ingredientes em ordem alfabetica, para o campo de escolha da ficha tecnica.
    // SELECT * FROM ingrediente ORDER BY nome
    List<Ingrediente> findAllByOrderByNomeAsc();
}
