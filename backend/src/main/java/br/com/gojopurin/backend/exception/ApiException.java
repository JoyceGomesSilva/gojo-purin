package br.com.gojopurin.backend.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

// Erro de regra de negocio. Quem lanca escolhe o codigo HTTP da resposta e,
// se quiser, uma lista de detalhes (ex.: os ingredientes que estao em falta).
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> detalhes;

    public ApiException(HttpStatus status, String mensagem) {
        this(status, mensagem, List.of());
    }

    public ApiException(HttpStatus status, String mensagem, List<String> detalhes) {
        super(mensagem);
        this.status = status;
        this.detalhes = List.copyOf(detalhes);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getDetalhes() {
        return detalhes;
    }
}
