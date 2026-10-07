package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.MovimentacaoRequest;
import br.com.gojopurin.backend.dto.MovimentacaoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.SaldoResponse;
import br.com.gojopurin.backend.service.AdminEstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Estoque no painel (RF-028, RF-030 a RF-033). So ADMIN e GERENTE.
@RestController
@RequestMapping("/api/admin/estoque")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminEstoqueController {

    private final AdminEstoqueService adminEstoqueService;

    public AdminEstoqueController(AdminEstoqueService adminEstoqueService) {
        this.adminEstoqueService = adminEstoqueService;
    }

    // Saldo atual de todos os ingredientes ativos.
    @GetMapping("/saldo")
    public List<SaldoResponse> saldo() {
        return adminEstoqueService.listarSaldos();
    }

    // So os ingredientes abaixo do estoque minimo.
    @GetMapping("/alertas")
    public List<SaldoResponse> alertas() {
        return adminEstoqueService.listarAlertas();
    }

    // Entrada (ajuste) ou saida (perda) manual. "autenticacao" traz quem esta
    // logado: o Spring preenche sozinho a partir do token.
    @PostMapping("/movimentacao")
    @ResponseStatus(HttpStatus.CREATED)
    public SaldoResponse movimentar(@RequestBody @Valid MovimentacaoRequest request, Authentication autenticacao) {
        return adminEstoqueService.movimentar(request, autenticacao.getName());
    }

    // GET /api/admin/estoque/movimentacoes?ingredienteId=4&tipo=SAIDA&page=0&size=20
    @GetMapping("/movimentacoes")
    public PaginaResponse<MovimentacaoResponse> movimentacoes(
            @RequestParam(required = false) Long ingredienteId,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int tamanho = Math.max(1, Math.min(size, 50));
        return adminEstoqueService.listarMovimentacoes(ingredienteId, tipo, Math.max(page, 0), tamanho);
    }
}
