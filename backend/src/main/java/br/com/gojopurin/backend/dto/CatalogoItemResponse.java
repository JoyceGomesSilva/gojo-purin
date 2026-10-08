package br.com.gojopurin.backend.dto;

import br.com.gojopurin.backend.model.FornecedorProduto;

import java.math.BigDecimal;

// Uma linha do catalogo de um fornecedor.
public record CatalogoItemResponse(
        Long id,
        Long ingredienteId,
        String ingredienteNome,
        String unidade,
        // Quanto este fornecedor cobra.
        BigDecimal preco,
        // Quanto o ingrediente custa hoje no sistema (o da ultima compra recebida).
        BigDecimal custoAtual
) {
    public static CatalogoItemResponse de(FornecedorProduto produto) {
        return new CatalogoItemResponse(
                produto.getId(),
                produto.getIngrediente().getId(),
                produto.getIngrediente().getNome(),
                produto.getUnidadeVenda(),
                produto.getPreco(),
                produto.getIngrediente().getCustoUnitario());
    }
}
