package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Usuario;

import java.time.LocalDateTime;

// Usuario interno como o painel ve. Repare que nao existe campo de senha
// aqui: nem a senha nem o hash saem da API (RNF01).
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String perfil,
        String telefone,
        String status,
        LocalDateTime criadoEm
) {
    // Converte a Entity (linha do banco) no DTO (o que vai no JSON).
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getTelefone(),
                usuario.getStatus(),
                usuario.getCreatedAt());
    }
}
