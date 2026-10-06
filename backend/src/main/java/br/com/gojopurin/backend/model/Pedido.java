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
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Um pedido feito por um cliente.
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    // RECEBIDO, CONFIRMADO, EM_PREPARO, PRONTO, SAIU_ENTREGA, FINALIZADO ou CANCELADO.
    @Column(nullable = false)
    private String status = "RECEBIDO";

    @Column(nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private String enderecoEntrega;

    private String observacoes;

    private String motivoCancelamento;

    @Column(nullable = false)
    private String canal = "SITE";

    // Pagamento simulado (RF-006): o pedido ja nasce marcado como pago.
    @Column(nullable = false)
    private boolean pago;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // cascade ALL: ao salvar o pedido, os itens e o historico sao salvos junto.
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<PedidoItem> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<PedidoStatusHistorico> historico = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void aoAtualizar() {
        updatedAt = LocalDateTime.now();
    }

    // Acrescenta um item ja ligando os dois lados da relacao.
    public void adicionarItem(PedidoItem item) {
        item.setPedido(this);
        itens.add(item);
    }

    // Muda o status e registra a mudanca na linha do tempo.
    public void registrarStatus(String novoStatus, Long usuarioId) {
        this.status = novoStatus;
        PedidoStatusHistorico registro = new PedidoStatusHistorico();
        registro.setPedido(this);
        registro.setStatus(novoStatus);
        registro.setUsuarioId(usuarioId);
        historico.add(registro);
    }

    public Long getId() { return id; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public String getStatus() { return status; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public boolean isPago() { return pago; }
    public void setPago(boolean pago) { this.pago = pago; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<PedidoItem> getItens() { return itens; }

    public List<PedidoStatusHistorico> getHistorico() { return historico; }
}
