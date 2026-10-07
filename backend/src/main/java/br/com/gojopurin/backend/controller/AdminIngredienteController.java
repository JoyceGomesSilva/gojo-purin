package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.IngredienteOpcaoResponse;
import br.com.gojopurin.backend.dto.IngredienteRequest;
import br.com.gojopurin.backend.dto.IngredienteResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.service.FichaTecnicaService;
import br.com.gojopurin.backend.service.IngredienteService;
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

// CRUD de ingredientes (RF-027). So ADMIN e GERENTE.
@RestController
@RequestMapping("/api/admin/ingredientes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminIngredienteController {

    private final IngredienteService ingredienteService;
    private final FichaTecnicaService fichaTecnicaService;

    public AdminIngredienteController(IngredienteService ingredienteService,
                                      FichaTecnicaService fichaTecnicaService) {
        this.ingredienteService = ingredienteService;
        this.fichaTecnicaService = fichaTecnicaService;
    }

    // GET /api/admin/ingredientes?status=ATIVO&busca=arroz&page=0&size=10
    @GetMapping
    public PaginaResponse<IngredienteResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return ingredienteService.listar(status, busca, Math.max(page, 0), tamanho);
    }

    // A lista resumida que a tela da ficha tecnica usa no campo de escolha.
    @GetMapping("/opcoes")
    public List<IngredienteOpcaoResponse> opcoes() {
        return fichaTecnicaService.listarIngredientes();
    }

    @GetMapping("/{id}")
    public IngredienteResponse detalhar(@PathVariable Long id) {
        return ingredienteService.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredienteResponse criar(@RequestBody @Valid IngredienteRequest request) {
        return ingredienteService.criar(request);
    }

    @PutMapping("/{id}")
    public IngredienteResponse editar(@PathVariable Long id, @RequestBody @Valid IngredienteRequest request) {
        return ingredienteService.editar(id, request);
    }

    // DELETE nao apaga a linha: desativa o ingrediente (RN06).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Long id) {
        ingredienteService.desativar(id);
    }
}
