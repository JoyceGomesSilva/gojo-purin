package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.AdminPedidoResponse;
import br.com.gojopurin.backend.dto.CancelarPedidoRequest;
import br.com.gojopurin.backend.dto.MudarStatusRequest;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.service.AdminPedidoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Pedidos no painel da cozinha. So a equipe interna entra (RF-040).
// As regras finas (o que o cozinheiro pode ou nao) ficam no service.
@RestController
@RequestMapping("/api/admin/pedidos")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'COZINHEIRO')")
public class AdminPedidoController {

    private final AdminPedidoService adminPedidoService;

    public AdminPedidoController(AdminPedidoService adminPedidoService) {
        this.adminPedidoService = adminPedidoService;
    }

    // GET /api/admin/pedidos?status=RECEBIDO&canal=SITE&de=2026-10-01&ate=2026-10-06&page=0&size=10
    @GetMapping
    public PaginaResponse<AdminPedidoResponse> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return adminPedidoService.listar(status, canal, de, ate, Math.max(page, 0), tamanho);
    }

    @GetMapping("/{id}")
    public AdminPedidoResponse detalhar(@PathVariable Long id) {
        return adminPedidoService.detalhar(id);
    }

    @PatchMapping("/{id}/status")
    public AdminPedidoResponse mudarStatus(@PathVariable Long id,
                                           @RequestBody @Valid MudarStatusRequest request,
                                           Authentication autenticacao) {
        return adminPedidoService.mudarStatus(id, request.status(), autenticacao.getName());
    }

    @PatchMapping("/{id}/cancelar")
    public AdminPedidoResponse cancelar(@PathVariable Long id,
                                        @RequestBody @Valid CancelarPedidoRequest request,
                                        Authentication autenticacao) {
        return adminPedidoService.cancelar(id, request.motivo(), autenticacao.getName());
    }
}
