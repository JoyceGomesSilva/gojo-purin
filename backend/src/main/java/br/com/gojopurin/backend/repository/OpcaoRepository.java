package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Opcao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcaoRepository extends JpaRepository<Opcao, Long> {
}
