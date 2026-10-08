package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.DashboardResumoResponse;
import br.com.gojopurin.backend.dto.TopPratoResponse;
import br.com.gojopurin.backend.dto.VendaDiaResponse;
import br.com.gojopurin.backend.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

// Dashboard (RF-034 a RF-037). So ADMIN e GERENTE.
// Os alertas de estoque (RF-036) vem de GET /api/admin/estoque/alertas.
@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Cards de hoje: faturamento, pedidos, ticket medio, food cost.
    @GetMapping("/resumo")
    public DashboardResumoResponse resumo() {
        return dashboardService.resumo();
    }

    // GET /api/admin/dashboard/top-pratos?de=2026-10-01&ate=2026-10-08
    // Sem datas: os ultimos 7 dias.
    @GetMapping("/top-pratos")
    public List<TopPratoResponse> topPratos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LocalDate fim = ate != null ? ate : hoje();
        return dashboardService.topPratos(de != null ? de : fim.minusDays(6), fim);
    }

    // GET /api/admin/dashboard/vendas?de=2026-10-01&ate=2026-10-08
    // Sem datas: os ultimos 7 dias.
    @GetMapping("/vendas")
    public List<VendaDiaResponse> vendas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        LocalDate fim = ate != null ? ate : hoje();
        return dashboardService.vendasPorDia(de != null ? de : fim.minusDays(6), fim);
    }

    private LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }
}
