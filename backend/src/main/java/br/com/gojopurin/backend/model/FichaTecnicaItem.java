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

// Uma linha da ficha tecnica: um ingrediente e a quantidade usada.
@Entity
@Table(name = "ficha_tecnica_item")
public class FichaTecnicaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_tecnica_id", nullable = false)
    private FichaTecnica fichaTecnica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingrediente_id", nullable = false)
    private Ingrediente ingrediente;

    // O que vai no prato.
    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(nullable = false)
    private String unidade;

    // Perda no preparo (casca, limpeza). 1.20 = gasta 20% a mais do que vai no prato.
    @Column(nullable = false)
    private BigDecimal fatorCorrecao = BigDecimal.ONE;

    public Long getId() { return id; }

    public FichaTecnica getFichaTecnica() { return fichaTecnica; }
    public void setFichaTecnica(FichaTecnica fichaTecnica) { this.fichaTecnica = fichaTecnica; }

    public Ingrediente getIngrediente() { return ingrediente; }
    public void setIngrediente(Ingrediente ingrediente) { this.ingrediente = ingrediente; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getFatorCorrecao() { return fatorCorrecao; }
    public void setFatorCorrecao(BigDecimal fatorCorrecao) { this.fatorCorrecao = fatorCorrecao; }
}
