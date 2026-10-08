package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.CompraRequest;
import br.com.gojopurin.backend.dto.CompraResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.RecebimentoRequest;
import br.com.gojopurin.backend.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

// Pedidos de compra (RF-024) e recebimento (RF-025). So ADMIN e GERENTE.
@RestController
@RequestMapping("/api/admin/compras")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminCompraController {

    private final CompraService compraService;

    public AdminCompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // GET /api/admin/compras?status=ENVIADO&fornecedorId=2&page=0&size=10
    @GetMapping
    public PaginaResponse<CompraResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return compraService.listar(status, fornecedorId, Math.max(page, 0), tamanho);
    }

    @GetMapping("/{id}")
    public CompraResponse detalhar(@PathVariable Long id) {
        return compraService.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse criar(@RequestBody @Valid CompraRequest request, Authentication autenticacao) {
        return compraService.criar(request, autenticacao.getName());
    }

    @PutMapping("/{id}")
    public CompraResponse editar(@PathVariable Long id, @RequestBody @Valid CompraRequest request) {
        return compraService.editar(id, request);
    }

    // DELETE nao apaga a linha: cancela o pedido (RN06).
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        compraService.cancelar(id);
    }

    @PatchMapping("/{id}/enviar")
    public CompraResponse enviar(@PathVariable Long id) {
        return compraService.enviar(id);
    }

    // O corpo e opcional (required = false): serve so para lote e validade.
    @PostMapping("/{id}/receber")
    public CompraResponse receber(@PathVariable Long id,
                                  @RequestBody(required = false) @Valid RecebimentoRequest request,
                                  Authentication autenticacao) {
        return compraService.receber(id, request, autenticacao.getName());
    }
}
