package br.com.gojopurin.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Uma linha da tabela "prato".
@Entity
@Table(name = "prato")
public class Prato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muitos pratos pertencem a uma categoria. A coluna categoria_id guarda qual.
    // LAZY: a categoria so e buscada no banco quando for usada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    private String fotoUrl;

    // Dinheiro sempre em BigDecimal: double erra centavos em contas.
    @Column(nullable = false)
    private BigDecimal precoVenda;

    @Column(nullable = false)
    private Integer tempoPreparoMin = 15;

    private String anime;

    private String personagem;

    // ATIVO, INATIVO ou PAUSADO.
    @Column(nullable = false)
    private String status = "INATIVO";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) { this.precoVenda = precoVenda; }

    public Integer getTempoPreparoMin() { return tempoPreparoMin; }
    public void setTempoPreparoMin(Integer tempoPreparoMin) { this.tempoPreparoMin = tempoPreparoMin; }

    public String getAnime() { return anime; }
    public void setAnime(String anime) { this.anime = anime; }

    public String getPersonagem() { return personagem; }
    public void setPersonagem(String personagem) { this.personagem = personagem; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
