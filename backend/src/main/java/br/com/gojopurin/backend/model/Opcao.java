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

// Uma resposta possivel dentro de um grupo ("Gema mole", "Ovo extra").
@Entity
@Table(name = "opcao")
public class Opcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_opcao_id", nullable = false)
    private GrupoOpcao grupoOpcao;

    @Column(nullable = false)
    private String nome;

    // Quanto a opcao soma ao preco do prato (zero para escolhas gratuitas).
    @Column(nullable = false)
    private BigDecimal precoAdicional = BigDecimal.ZERO;

    // Um adicional pode consumir estoque (ex.: "Ovo extra" gasta 1 ovo).
    // Vazio quando a opcao nao gasta nada (ex.: "Sem gelo").
    private Long ingredienteId;

    private BigDecimal quantidadeIngrediente;

    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(nullable = false)
    private String status = "ATIVO";

    public Long getId() { return id; }

    public GrupoOpcao getGrupoOpcao() { return grupoOpcao; }
    public void setGrupoOpcao(GrupoOpcao grupoOpcao) { this.grupoOpcao = grupoOpcao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPrecoAdicional() { return precoAdicional; }
    public void setPrecoAdicional(BigDecimal precoAdicional) { this.precoAdicional = precoAdicional; }

    public Long getIngredienteId() { return ingredienteId; }
    public void setIngredienteId(Long ingredienteId) { this.ingredienteId = ingredienteId; }

    public BigDecimal getQuantidadeIngrediente() { return quantidadeIngrediente; }
    public void setQuantidadeIngrediente(BigDecimal quantidadeIngrediente) { this.quantidadeIngrediente = quantidadeIngrediente; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
