package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.PratoGrupoOpcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PratoGrupoOpcaoRepository extends JpaRepository<PratoGrupoOpcao, Long> {

    // Os grupos de complementos ativos de um prato, na ordem de exibicao.
    @Query("""
            select pg from PratoGrupoOpcao pg join fetch pg.grupoOpcao g
            where pg.prato.id = :pratoId and g.status = 'ATIVO'
            order by pg.ordem
            """)
    List<PratoGrupoOpcao> buscarPorPrato(@Param("pratoId") Long pratoId);
}
