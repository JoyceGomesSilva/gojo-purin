package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.FornecedorPrecoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorPrecoHistoricoRepository extends JpaRepository<FornecedorPrecoHistorico, Long> {

    // Todos os precos ja registrados para um ingrediente, do mais antigo ao mais novo.
    List<FornecedorPrecoHistorico> findByIngredienteIdOrderByCreatedAtAscIdAsc(Long ingredienteId);
}
