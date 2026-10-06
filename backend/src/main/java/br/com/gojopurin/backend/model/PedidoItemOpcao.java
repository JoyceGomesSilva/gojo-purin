package br.com.gojopurin.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

// Um complemento escolhido em uma linha do pedido. Nome e preco sao copiados
// do cardapio: se a opcao mudar depois, o pedido antigo nao muda.
@Entity
@Table(name = "pedido_item_opcao")
public class PedidoItemOpcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_item_id", nullable = false)
    private PedidoItem pedidoItem;

    @Column(nullable = false)
    private Long opcaoId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal precoAdicional = BigDecimal.ZERO;

    public Long getId() { return id; }

    public PedidoItem getPedidoItem() { return pedidoItem; }
    public void setPedidoItem(PedidoItem pedidoItem) { this.pedidoItem = pedidoItem; }

    public Long getOpcaoId() { return opcaoId; }
    public void setOpcaoId(Long opcaoId) { this.opcaoId = opcaoId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPrecoAdicional() { return precoAdicional; }
    public void setPrecoAdicional(BigDecimal precoAdicional) { this.precoAdicional = precoAdicional; }
}
