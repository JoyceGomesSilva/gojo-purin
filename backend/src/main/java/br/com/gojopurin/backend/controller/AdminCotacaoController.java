package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.CotacaoResponse;
import br.com.gojopurin.backend.service.FornecedorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Cotacao comparativa (RF-023) com o historico de precos (RF-026).
@RestController
@RequestMapping("/api/admin/cotacao")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminCotacaoController {

    private final FornecedorService fornecedorService;

    public AdminCotacaoController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping("/{ingredienteId}")
    public CotacaoResponse cotar(@PathVariable Long ingredienteId) {
        return fornecedorService.cotar(ingredienteId);
    }
}
