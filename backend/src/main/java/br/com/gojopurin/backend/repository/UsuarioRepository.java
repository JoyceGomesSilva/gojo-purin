package br.com.gojopurin.backend.repository;

import br.com.gojopurin.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// O Spring cria a implementacao sozinho. Pelo nome do metodo ele monta o SQL:
// findByEmail -> SELECT * FROM usuario WHERE email = ?
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
