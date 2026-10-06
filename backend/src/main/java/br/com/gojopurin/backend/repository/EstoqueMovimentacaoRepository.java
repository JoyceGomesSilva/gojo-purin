package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.EstoqueMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EstoqueMovimentacaoRepository extends JpaRepository<EstoqueMovimentacao, Long> {

    // Saldo de cada ingrediente pedido: entradas e estornos somam, saidas subtraem.
    // Cada linha do resultado traz [id do ingrediente, saldo].
    @Query("""
            select m.ingredienteId,
                   sum(case when m.tipo = 'SAIDA' then (m.quantidade * -1) else m.quantidade end)
            from EstoqueMovimentacao m
            where m.ingredienteId in :ingredienteIds
            group by m.ingredienteId
            """)
    List<Object[]> somarSaldos(@Param("ingredienteIds") Collection<Long> ingredienteIds);

    // As movimentacoes de um tipo geradas por um pedido (ex.: as SAIDAS da baixa).
    List<EstoqueMovimentacao> findByPedidoIdAndTipo(Long pedidoId, String tipo);
}
