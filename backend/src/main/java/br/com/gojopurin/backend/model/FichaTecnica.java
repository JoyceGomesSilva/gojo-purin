package br.com.gojopurin.backend.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// A receita de um prato: quais ingredientes ele usa e em que quantidade.
// Cada prato tem no maximo uma ficha (prato 1:1 ficha_tecnica).
@Entity
@Table(name = "ficha_tecnica")
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prato_id", nullable = false, unique = true)
    private Prato prato;

    // Quantas porcoes a receita rende.
    @Column(nullable = false)
    private Integer rendimento = 1;

    // Instrucoes passo a passo para a cozinha (RF-014). Texto livre, pode ser longo.
    private String modoPreparo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // cascade: ao salvar a ficha, os itens da lista sao salvos junto.
    // orphanRemoval: um item tirado da lista e apagado do banco.
    // Assim o service so precisa mexer na lista.
    @OneToMany(mappedBy = "fichaTecnica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FichaTecnicaItem> itens = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Prato getPrato() { return prato; }
    public void setPrato(Prato prato) { this.prato = prato; }

    public Integer getRendimento() { return rendimento; }
    public void setRendimento(Integer rendimento) { this.rendimento = rendimento; }

    public String getModoPreparo() { return modoPreparo; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<FichaTecnicaItem> getItens() { return itens; }
}
