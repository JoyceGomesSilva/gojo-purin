package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.CatalogoItemRequest;
import br.com.gojopurin.backend.dto.CatalogoItemResponse;
import br.com.gojopurin.backend.dto.FornecedorRequest;
import br.com.gojopurin.backend.dto.FornecedorResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.service.FornecedorService;
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

import java.util.List;

// CRUD de fornecedores e o catalogo de cada um (RF-021 e RF-022).
// So ADMIN e GERENTE: o cozinheiro nao acessa fornecedores (RF-043).
@RestController
@RequestMapping("/api/admin/fornecedores")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminFornecedorController {

    private final FornecedorService fornecedorService;

    public AdminFornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    // GET /api/admin/fornecedores?status=ATIVO&busca=vale&page=0&size=10
    @GetMapping
    public PaginaResponse<FornecedorResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return fornecedorService.listar(status, busca, Math.max(page, 0), tamanho);
    }

    // Os fornecedores ativos, para o campo de escolha da tela de compras.
    @GetMapping("/opcoes")
    public List<FornecedorResponse> opcoes() {
        return fornecedorService.listarAtivos();
    }

    @GetMapping("/{id}")
    public FornecedorResponse detalhar(@PathVariable Long id) {
        return fornecedorService.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponse criar(@RequestBody @Valid FornecedorRequest request) {
        return fornecedorService.criar(request);
    }

    @PutMapping("/{id}")
    public FornecedorResponse editar(@PathVariable Long id, @RequestBody @Valid FornecedorRequest request) {
        return fornecedorService.editar(id, request);
    }

    // DELETE nao apaga a linha: desativa o fornecedor (RN06).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        fornecedorService.desativar(id);
    }

    @GetMapping("/{id}/catalogo")
    public List<CatalogoItemResponse> catalogo(@PathVariable Long id) {
        return fornecedorService.listarCatalogo(id);
    }

    // Coloca um ingrediente no catalogo ou atualiza o preco dele.
    @PutMapping("/{id}/catalogo")
    public CatalogoItemResponse salvarNoCatalogo(@PathVariable Long id, @RequestBody @Valid CatalogoItemRequest request) {
        return fornecedorService.salvarNoCatalogo(id, request);
    }
}
