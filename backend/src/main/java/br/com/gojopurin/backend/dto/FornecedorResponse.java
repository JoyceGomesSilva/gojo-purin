package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.Fornecedor;

// Fornecedor como o painel ve. O CNPJ vai so com os 14 caracteres;
// a tela coloca a pontuacao.
public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String telefone,
        String email,
        String categoriasProdutos,
        String status
) {
    public static FornecedorResponse de(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getRazaoSocial(),
                fornecedor.getCnpj(),
                fornecedor.getTelefone(),
                fornecedor.getEmail(),
                fornecedor.getCategoriasProdutos(),
                fornecedor.getStatus());
    }
}
