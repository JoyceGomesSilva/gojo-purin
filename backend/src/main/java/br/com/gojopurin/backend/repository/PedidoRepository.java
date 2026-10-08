package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// JpaSpecificationExecutor permite montar filtros opcionais (status, canal,
// datas) em tempo de execucao, usados na lista de pedidos do painel.
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    // Historico do cliente, do mais recente para o mais antigo (RF-008).
    Page<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId, Pageable pageable);

    // Busca o pedido so se ele for daquele cliente: ninguem ve pedido alheio.
    Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);

    // ---------- Dashboard (RF-034 a RF-037) ----------
    // Pedidos cancelados nao contam como venda em nenhuma destas consultas.

    // Quantos pedidos e quanto faturaram no periodo. Uma linha: [quantidade, soma].
    // coalesce troca o "nulo" da soma de nenhum pedido por zero.
    @Query("""
            select count(p), coalesce(sum(p.valorTotal), 0)
            from Pedido p
            where p.status <> 'CANCELADO' and p.createdAt >= :inicio and p.createdAt < :fim
            """)
    List<Object[]> resumir(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Os pratos mais vendidos no periodo. Cada linha: [id, nome, unidades, faturamento].
    // O Pageable limita quantas linhas voltam (as 5 primeiras, no top 5).
    @Query("""
            select i.prato.id, i.prato.nome, sum(i.quantidade), sum(i.precoUnitario * i.quantidade)
            from PedidoItem i
            where i.pedido.status <> 'CANCELADO'
              and i.pedido.createdAt >= :inicio and i.pedido.createdAt < :fim
            group by i.prato.id, i.prato.nome
            order by sum(i.quantidade) desc, i.prato.nome
            """)
    List<Object[]> maisVendidos(@Param("inicio") LocalDateTime inicio,
                                @Param("fim") LocalDateTime fim,
                                Pageable pageable);

    // Data e valor de cada pedido do periodo. O agrupamento por dia e feito
    // no service, porque depende do fuso horario. Cada linha: [data, valor].
    @Query("""
            select p.createdAt, p.valorTotal
            from Pedido p
            where p.status <> 'CANCELADO' and p.createdAt >= :inicio and p.createdAt < :fim
            """)
    List<Object[]> vendasNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
