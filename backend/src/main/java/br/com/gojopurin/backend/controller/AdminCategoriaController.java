package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.AdminCategoriaRequest;
import br.com.gojopurin.backend.dto.AdminCategoriaResponse;
import br.com.gojopurin.backend.service.CategoriaService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// CRUD de categorias no painel (RF-009). So ADMIN e GERENTE: o cozinheiro
// nao mexe no cardapio.
@RestController
@RequestMapping("/api/admin/categorias")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminCategoriaController {

    private final CategoriaService categoriaService;

    public AdminCategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<AdminCategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCategoriaResponse criar(@RequestBody @Valid AdminCategoriaRequest request) {
        return categoriaService.criar(request);
    }

    @PutMapping("/{id}")
    public AdminCategoriaResponse editar(@PathVariable Long id, @RequestBody @Valid AdminCategoriaRequest request) {
        return categoriaService.editar(id, request);
    }

    // DELETE nao apaga a linha: desativa a categoria (RN06).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        categoriaService.desativar(id);
    }
}
