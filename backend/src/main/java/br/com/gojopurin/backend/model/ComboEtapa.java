package br.com.gojopurin.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

// Uma escolha dentro do combo ("Prato", "Bebida", "Sobremesa").
@Entity
@Table(name = "combo_etapa")
public class ComboEtapa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @Column(nullable = false)
    private String nome;

    // Etapa opcional pode ser pulada pelo cliente.
    @Column(nullable = false)
    private boolean obrigatoria = true;

    @Column(nullable = false)
    private Integer ordem = 0;

    // Os pratos que podem ser escolhidos nesta etapa. A ligacao fica na
    // tabela combo_etapa_prato, que so tem os dois ids.
    @ManyToMany
    @JoinTable(
            name = "combo_etapa_prato",
            joinColumns = @JoinColumn(name = "combo_etapa_id"),
            inverseJoinColumns = @JoinColumn(name = "prato_id"))
    private Set<Prato> pratos = new HashSet<>();

    public Long getId() { return id; }

    public Combo getCombo() { return combo; }
    public void setCombo(Combo combo) { this.combo = combo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isObrigatoria() { return obrigatoria; }
    public void setObrigatoria(boolean obrigatoria) { this.obrigatoria = obrigatoria; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Set<Prato> getPratos() { return pratos; }
}
