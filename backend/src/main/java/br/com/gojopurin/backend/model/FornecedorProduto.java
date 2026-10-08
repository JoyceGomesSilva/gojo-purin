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

// Catalogo do fornecedor (RF-022): "este fornecedor vende este ingrediente
// por este preco". E a tabela do meio da ligacao N:N fornecedor-ingrediente.
@Entity
@Table(name = "fornecedor_produto")
public class FornecedorProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    // Preco de UMA unidade de venda (ex.: R$ 0,012 por grama).
    @Column(nullable = false)
    private BigDecimal preco;

    // G, ML, UN, KG ou L. Sempre igual a unidade padrao do ingrediente.
    @Column(nullable = false)
    private String unidadeVenda;

    public Long getId() { return id; }

    public Fornecedor getFornecedor() { return fornecedor; }
    public void setFornecedor(Fornecedor fornecedor) { this.fornecedor = fornecedor; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getUnidadeVenda() { return unidadeVenda; }
    public void setUnidadeVenda(String unidadeVenda) { this.unidadeVenda = unidadeVenda; }
}
