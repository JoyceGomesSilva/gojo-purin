package br.com.gojopurin.backend.model;

// Os motivos aceitos em uma saida manual de estoque (RF-030).
// Enum = lista fechada: qualquer valor fora dela e recusado.
public enum MotivoPerda {
    DESPERDICIO,
    VENCIMENTO,
    QUEBRA,
    USO_INTERNO
}
