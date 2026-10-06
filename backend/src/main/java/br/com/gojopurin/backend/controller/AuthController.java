package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.AuthResponse;
import br.com.gojopurin.backend.dto.LoginRequest;
import br.com.gojopurin.backend.dto.RegisterRequest;
import br.com.gojopurin.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Endpoints publicos de autenticacao. Sem regra de negocio aqui: so recebe,
// chama o service e devolve.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @Valid dispara as validacoes do DTO antes de entrar no metodo.
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@RequestBody @Valid RegisterRequest request) {
        return authService.registrar(request);
    }
}
