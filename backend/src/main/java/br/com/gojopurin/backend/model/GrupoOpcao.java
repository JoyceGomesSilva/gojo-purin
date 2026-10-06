package br.com.gojopurin.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Um grupo de complementos: uma pergunta feita ao cliente sobre o prato
// ("Ponto do ovo", "Adicionais"). O mesmo grupo pode servir a varios pratos.
@Entity
@Table(name = "grupo_opcao")
public class GrupoOpcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // Se true, o cliente precisa responder antes de adicionar ao carrinho.
    @Column(nullable = false)
    private boolean obrigatorio;

    @Column(nullable = false)
    private Integer minEscolhas = 0;

    @Column(nullable = false)
    private Integer maxEscolhas = 1;

    @Column(nullable = false)
    private String status = "ATIVO";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Um grupo tem varias opcoes. "mappedBy" diz que a coluna da ligacao
    // (grupo_opcao_id) fica na tabela opcao.
    @OneToMany(mappedBy = "grupoOpcao")
    @OrderBy("ordem asc")
    private List<Opcao> opcoes = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public boolean isObrigatorio() { return obrigatorio; }
    public void setObrigatorio(boolean obrigatorio) { this.obrigatorio = obrigatorio; }

    public Integer getMinEscolhas() { return minEscolhas; }
    public void setMinEscolhas(Integer minEscolhas) { this.minEscolhas = minEscolhas; }

    public Integer getMaxEscolhas() { return maxEscolhas; }
    public void setMaxEscolhas(Integer maxEscolhas) { this.maxEscolhas = maxEscolhas; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Opcao> getOpcoes() { return opcoes; }
}
