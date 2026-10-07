package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O que o painel envia para criar um usuario interno (RF-041).
// O perfil so aceita GERENTE ou COZINHEIRO: cliente se cadastra sozinho pelo
// site e o admin vem do seed.
public record UsuarioCriarRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "Nome muito longo")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "E-mail muito longo")
        String email,

        @NotBlank(message = "Informe a senha")
        @Size(min = 6, max = 60, message = "A senha deve ter de 6 a 60 caracteres")
        String senha,

        @NotBlank(message = "Informe o perfil")
        @Pattern(regexp = "GERENTE|COZINHEIRO", message = "O perfil deve ser GERENTE ou COZINHEIRO")
        String perfil,

        // Opcional: pode vir vazio.
        @Size(max = 20, message = "Telefone muito longo")
        String telefone
) {
}
