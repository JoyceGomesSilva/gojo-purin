package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.validation.Cnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O que o painel envia para criar ou editar um fornecedor (RF-021).
public record FornecedorRequest(
        @NotBlank(message = "Informe a razão social")
        @Size(max = 160, message = "Razão social muito longa")
        String razaoSocial,

        // Aceita com ou sem pontuacao. @Cnpj confere os digitos verificadores (RN07).
        @NotBlank(message = "Informe o CNPJ")
        @Cnpj
        String cnpj,

        @Size(max = 20, message = "Telefone muito longo")
        String telefone,

        @Email(message = "E-mail inválido")
        @Size(max = 160, message = "E-mail muito longo")
        String email,

        // Texto livre: "Legumes, verduras, frutas".
        @Size(max = 255, message = "Categorias muito longas")
        String categoriasProdutos,

        @NotBlank(message = "Informe o status")
        @Pattern(regexp = "ATIVO|INATIVO", message = "O status deve ser ATIVO ou INATIVO")
        String status
) {
}
