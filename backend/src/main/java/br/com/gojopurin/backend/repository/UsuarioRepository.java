package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// O Spring cria a implementacao sozinho. Pelo nome do metodo ele monta o SQL:
// findByEmail -> SELECT * FROM usuario WHERE email = ?
// JpaSpecificationExecutor permite montar os filtros opcionais (perfil e
// status) da lista de usuarios do painel.
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // "Existe OUTRO usuario com este e-mail?" Usado na edicao, para o proprio
    // usuario nao esbarrar no e-mail que ja e dele.
    // SELECT ... WHERE email = ? AND id <> ?
    boolean existsByEmailAndIdNot(String email, Long id);
}
