package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// JpaSpecificationExecutor permite montar filtros opcionais (status, canal,
// datas) em tempo de execucao, usados na lista de pedidos do painel.
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    // Historico do cliente, do mais recente para o mais antigo (RF-008).
    Page<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId, Pageable pageable);

    // Busca o pedido so se ele for daquele cliente: ninguem ve pedido alheio.
    Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);
}
