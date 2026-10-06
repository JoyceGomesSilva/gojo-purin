package br.com.gojopurin.backend.dto;

// O que o back devolve depois do login ou do cadastro.
// Repare que a senha nao aparece aqui (RNF01).
public record AuthResponse(
        String token,
        Long id,
        String nome,
        String email,
        String perfil
) {
}
