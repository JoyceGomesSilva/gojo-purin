package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// JpaSpecificationExecutor serve a lista do painel, que tem filtros opcionais.
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long>, JpaSpecificationExecutor<Fornecedor> {

    // O CNPJ e unico.
    boolean existsByCnpj(String cnpj);

    // A mesma pergunta na edicao, sem contar o proprio fornecedor.
    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    // Os ativos em ordem alfabetica, para os campos de escolha das telas.
    List<Fornecedor> findByStatusOrderByRazaoSocialAsc(String status);
}
