package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.FichaTecnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long> {

    // As fichas de varios pratos de uma vez, ja com os itens e os ingredientes.
    @Query("""
            select distinct f from FichaTecnica f
            join fetch f.itens i
            join fetch i.ingrediente
            where f.prato.id in :pratoIds
            """)
    List<FichaTecnica> buscarPorPratos(@Param("pratoIds") Collection<Long> pratoIds);

    // A ficha de um prato, se existir.
    // SELECT * FROM ficha_tecnica WHERE prato_id = ?
    Optional<FichaTecnica> findByPratoId(Long pratoId);

    // Para a lista de pratos do painel. "left join" traz tambem as fichas que
    // ainda nao tem nenhum ingrediente (a consulta de cima ignora essas).
    @Query("""
            select distinct f from FichaTecnica f
            left join fetch f.itens
            where f.prato.id in :pratoIds
            """)
    List<FichaTecnica> buscarComItensPorPratos(@Param("pratoIds") Collection<Long> pratoIds);
}
