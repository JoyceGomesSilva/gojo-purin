package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// O que o front envia no cadastro de cliente (RF-004).
public record RegisterRequest(
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

        @NotBlank(message = "Informe o telefone")
        @Size(max = 20, message = "Telefone muito longo")
        String telefone,

        @NotBlank(message = "Informe o endereço de entrega")
        @Size(max = 255, message = "Endereço muito longo")
        String endereco
) {
}
