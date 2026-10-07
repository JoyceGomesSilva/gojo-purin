package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.AdminPratoRequest;
import br.com.gojopurin.backend.dto.AdminPratoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.service.PratoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// CRUD de pratos no painel (RF-010). So ADMIN e GERENTE.
@RestController
@RequestMapping("/api/admin/pratos")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminPratoController {

    private final PratoService pratoService;

    public AdminPratoController(PratoService pratoService) {
        this.pratoService = pratoService;
    }

    // GET /api/admin/pratos?categoriaId=1&status=ATIVO&busca=ramen&page=0&size=10
    @GetMapping
    public PaginaResponse<AdminPratoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return pratoService.listar(categoriaId, status, busca, Math.max(page, 0), tamanho);
    }

    @GetMapping("/{id}")
    public AdminPratoResponse detalhar(@PathVariable Long id) {
        return pratoService.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminPratoResponse criar(@RequestBody @Valid AdminPratoRequest request) {
        return pratoService.criar(request);
    }

    @PutMapping("/{id}")
    public AdminPratoResponse editar(@PathVariable Long id, @RequestBody @Valid AdminPratoRequest request) {
        return pratoService.editar(id, request);
    }

    // DELETE nao apaga a linha: desativa o prato (RN06).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        pratoService.desativar(id);
    }
}
