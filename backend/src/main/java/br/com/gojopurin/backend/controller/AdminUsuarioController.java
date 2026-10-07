package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.UsuarioCriarRequest;
import br.com.gojopurin.backend.dto.UsuarioEditarRequest;
import br.com.gojopurin.backend.dto.UsuarioResponse;
import br.com.gojopurin.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// CRUD de usuarios internos (RF-041). So o ADMIN entra (RF-040).
// Sem regra de negocio aqui: so recebe, chama o service e devolve.
@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // GET /api/admin/usuarios?perfil=COZINHEIRO&status=ATIVO&page=0&size=10
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return usuarioService.listar(perfil, status, Math.max(page, 0), tamanho);
    }

    @GetMapping("/{id}")
    public UsuarioResponse detalhar(@PathVariable Long id) {
        return usuarioService.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@RequestBody @Valid UsuarioCriarRequest request) {
        return usuarioService.criar(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse editar(@PathVariable Long id, @RequestBody @Valid UsuarioEditarRequest request) {
        return usuarioService.editar(id, request);
    }

    // DELETE nao apaga a linha: desativa o usuario (soft delete).
    // 204 = "deu certo e nao tenho nada para devolver".
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
    }

    @PatchMapping("/{id}/reativar")
    public UsuarioResponse reativar(@PathVariable Long id) {
        return usuarioService.reativar(id);
    }
}
