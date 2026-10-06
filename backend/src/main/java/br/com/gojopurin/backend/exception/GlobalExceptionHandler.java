package br.com.gojopurin.backend.exception;

import br.com.gojopurin.backend.dto.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

// Captura os erros lancados em qualquer controller e devolve um JSON padrao,
// em vez de mostrar o erro tecnico do Java para o cliente.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros de regra de negocio (ApiException).
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> tratarApiException(ApiException ex) {
        ErroResponse corpo = new ErroResponse(
                ex.getStatus().value(), ex.getMessage(), List.of(), LocalDateTime.now());
        return ResponseEntity.status(ex.getStatus()).body(corpo);
    }

    // Erros de validacao dos DTOs (@NotBlank, @Email, @Size...).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        ErroResponse corpo = new ErroResponse(
                HttpStatus.BAD_REQUEST.value(), "Dados inválidos", detalhes, LocalDateTime.now());
        return ResponseEntity.badRequest().body(corpo);
    }
}
