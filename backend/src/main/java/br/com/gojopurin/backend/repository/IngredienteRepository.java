package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// JpaSpecificationExecutor serve a lista do painel, que tem filtros opcionais.
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long>, JpaSpecificationExecutor<Ingrediente> {

    // So os ativos, em ordem alfabetica (tela de saldo e alertas).
    List<Ingrediente> findByStatusOrderByNomeAsc(String status);

    // "Ja existe ingrediente com este SKU?" (o SKU e unico).
    boolean existsBySkuIgnoreCase(String sku);

    // A mesma pergunta na edicao, sem contar o proprio ingrediente.
    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    // Todos os ingredientes em ordem alfabetica, para o campo de escolha da ficha tecnica.
    // SELECT * FROM ingrediente ORDER BY nome
    List<Ingrediente> findAllByOrderByNomeAsc();
}
