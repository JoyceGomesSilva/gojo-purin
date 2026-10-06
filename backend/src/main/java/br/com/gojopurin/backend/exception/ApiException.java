package br.com.gojopurin.backend.exception;

import org.springframework.http.HttpStatus;

// Erro de regra de negocio. Quem lanca escolhe o codigo HTTP da resposta.
// Exemplo: throw new ApiException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
