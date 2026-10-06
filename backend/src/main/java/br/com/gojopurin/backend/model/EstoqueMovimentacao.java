package br.com.gojopurin.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Cada entrada ou saida de um ingrediente no estoque.
// O saldo de um ingrediente e a soma das ENTRADAS e ESTORNOS menos as SAIDAS.
@Entity
@Table(name = "estoque_movimentacao")
public class EstoqueMovimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ingredienteId;

    // ENTRADA, SAIDA ou ESTORNO.
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private BigDecimal quantidade;

    // COMPRA, VENDA, DESPERDICIO, VENCIMENTO, QUEBRA, USO_INTERNO, AJUSTE ou ESTORNO.
    @Column(nullable = false)
    private String motivo;

    private String lote;

    private LocalDate validade;

    private BigDecimal custoUnitario;

    // De onde veio a movimentacao (uma compra ou um pedido), quando houver.
    private Long pedidoCompraId;

    private Long pedidoId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(Long ingredienteId) { this.ingredienteId = ingredienteId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }

    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public void setCustoUnitario(BigDecimal custoUnitario) { this.custoUnitario = custoUnitario; }

    public Long getPedidoCompraId() { return pedidoCompraId; }
    public void setPedidoCompraId(Long pedidoCompraId) { this.pedidoCompraId = pedidoCompraId; }

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
