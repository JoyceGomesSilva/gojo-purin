package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Historico do cliente, do mais recente para o mais antigo (RF-008).
    Page<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId, Pageable pageable);

    // Busca o pedido so se ele for daquele cliente: ninguem ve pedido alheio.
    Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);
}
