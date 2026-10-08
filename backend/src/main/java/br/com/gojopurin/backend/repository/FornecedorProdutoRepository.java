package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.FornecedorProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FornecedorProdutoRepository extends JpaRepository<FornecedorProduto, Long> {

    // O catalogo de um fornecedor, em ordem alfabetica de ingrediente.
    @Query("""
            select fp from FornecedorProduto fp join fetch fp.ingrediente i
            where fp.fornecedor.id = :fornecedorId
            order by i.nome
            """)
    List<FornecedorProduto> buscarCatalogo(@Param("fornecedorId") Long fornecedorId);

    // RF-023: quem vende este ingrediente, do mais barato para o mais caro.
    // So fornecedores ativos entram na cotacao.
    @Query("""
            select fp from FornecedorProduto fp join fetch fp.fornecedor f
            where fp.ingrediente.id = :ingredienteId and f.status = 'ATIVO'
            order by fp.preco, f.razaoSocial
            """)
    List<FornecedorProduto> buscarCotacao(@Param("ingredienteId") Long ingredienteId);

    // SELECT ... WHERE fornecedor_id = ? AND ingrediente_id = ?
    Optional<FornecedorProduto> findByFornecedorIdAndIngredienteId(Long fornecedorId, Long ingredienteId);
}
