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
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Uma linha do pedido: um prato, a quantidade e os precos do momento da compra.
@Entity
@Table(name = "pedido_item")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prato_id", nullable = false)
    private Prato prato;

    @Column(nullable = false)
    private Integer quantidade;

    // Preco do prato no cardapio quando o pedido foi feito.
    @Column(nullable = false)
    private BigDecimal precoBase;

    // Desconto do combo (0 quando o prato foi pedido avulso).
    @Column(nullable = false)
    private BigDecimal descontoPercentual = BigDecimal.ZERO;

    // Valor final de uma unidade: precoBase com desconto + complementos.
    @Column(nullable = false)
    private BigDecimal precoUnitario;

    private String observacoes;

    // Preenchidos quando a linha veio de um combo.
    private Long comboId;

    private String comboNome;

    // Identifica cada combo dentro do pedido, para agrupar as linhas dele.
    private String comboChave;

    @OneToMany(mappedBy = "pedidoItem", cascade = CascadeType.ALL)
    @OrderBy("id asc")
    private List<PedidoItemOpcao> opcoes = new ArrayList<>();

    public void adicionarOpcao(PedidoItemOpcao opcao) {
        opcao.setPedidoItem(this);
        opcoes.add(opcao);
    }

    // quantidade x precoUnitario
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() { return id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Prato getPrato() { return prato; }
    public void setPrato(Prato prato) { this.prato = prato; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }

    public BigDecimal getDescontoPercentual() { return descontoPercentual; }
    public void setDescontoPercentual(BigDecimal descontoPercentual) { this.descontoPercentual = descontoPercentual; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getComboId() { return comboId; }
    public void setComboId(Long comboId) { this.comboId = comboId; }

    public String getComboNome() { return comboNome; }
    public void setComboNome(String comboNome) { this.comboNome = comboNome; }

    public String getComboChave() { return comboChave; }
    public void setComboChave(String comboChave) { this.comboChave = comboChave; }

    public List<PedidoItemOpcao> getOpcoes() { return opcoes; }
}
