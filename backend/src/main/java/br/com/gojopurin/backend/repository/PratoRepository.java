package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Prato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Consultas do cardapio publico. Todas trazem so pratos ATIVOS (RN09).
// "join fetch" busca o prato e a categoria dele na mesma consulta.
// Como a lista e paginada, o Spring tambem precisa de uma consulta de contagem.
public interface PratoRepository extends JpaRepository<Prato, Long> {

    @Query(value = """
            select p from Prato p join fetch p.categoria c
            where p.status = 'ATIVO' and c.status = 'ATIVO'
            order by c.ordem, p.nome
            """,
            countQuery = """
            select count(p) from Prato p join p.categoria c
            where p.status = 'ATIVO' and c.status = 'ATIVO'
            """)
    Page<Prato> buscarAtivos(Pageable pageable);

    @Query(value = """
            select p from Prato p join fetch p.categoria c
            where p.status = 'ATIVO' and c.status = 'ATIVO' and c.id = :categoriaId
            order by p.nome
            """,
            countQuery = """
            select count(p) from Prato p join p.categoria c
            where p.status = 'ATIVO' and c.status = 'ATIVO' and c.id = :categoriaId
            """)
    Page<Prato> buscarAtivosPorCategoria(@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query("""
            select p from Prato p join fetch p.categoria c
            where p.id = :id and p.status = 'ATIVO' and c.status = 'ATIVO'
            """)
    Optional<Prato> buscarAtivoPorId(@Param("id") Long id);
}
