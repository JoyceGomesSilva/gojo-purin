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

// Liga um prato a um grupo de complementos e guarda a ordem em que o grupo
// aparece na tela daquele prato.
@Entity
@Table(name = "prato_grupo_opcao")
public class PratoGrupoOpcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prato_id", nullable = false)
    private Prato prato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_opcao_id", nullable = false)
    private GrupoOpcao grupoOpcao;

    @Column(nullable = false)
    private Integer ordem = 0;

    public Long getId() { return id; }

    public Prato getPrato() { return prato; }
    public void setPrato(Prato prato) { this.prato = prato; }

    public GrupoOpcao getGrupoOpcao() { return grupoOpcao; }
    public void setGrupoOpcao(GrupoOpcao grupoOpcao) { this.grupoOpcao = grupoOpcao; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
}
