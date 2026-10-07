package br.com.gojopurin.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O que o painel envia para editar um usuario interno (RF-041).
// A diferenca para o de criar: aqui a senha e opcional. Se vier vazia (null),
// a senha atual continua valendo.
public record UsuarioEditarRequest(
        @NotBlank(message = "Informe o nome")
        @Size(max = 120, message = "Nome muito longo")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "E-mail muito longo")
        String email,

        // Sem @NotBlank: null passa. Se vier preenchida, precisa ter de 6 a 60.
        @Size(min = 6, max = 60, message = "A senha deve ter de 6 a 60 caracteres")
        String senha,

        @NotBlank(message = "Informe o perfil")
        @Pattern(regexp = "GERENTE|COZINHEIRO", message = "O perfil deve ser GERENTE ou COZINHEIRO")
        String perfil,

        @Size(max = 20, message = "Telefone muito longo")
        String telefone
) {
}
