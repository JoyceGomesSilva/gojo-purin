package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComboRepository extends JpaRepository<Combo, Long> {

    List<Combo> findByStatusOrderByOrdemAscNomeAsc(String status);

    Optional<Combo> findByIdAndStatus(Long id, String status);
}
