package br.com.gojopurin.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Um pedido de compra para um fornecedor (RF-024).
// Ciclo: RASCUNHO > ENVIADO > RECEBIDO. Tambem pode ser CANCELADO.
@Entity
@Table(name = "pedido_compra")
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private String status = "RASCUNHO";

    @Column(nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // Quem criou o pedido.
    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // cascade + orphanRemoval: os itens sao salvos e apagados junto com a lista.
    @OneToMany(mappedBy = "pedidoCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoCompraItem> itens = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<PedidoCompraItem> getItens() { return itens; }
}
