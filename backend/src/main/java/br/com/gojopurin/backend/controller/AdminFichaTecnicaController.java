package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.CustoResponse;
import br.com.gojopurin.backend.dto.FichaTecnicaRequest;
import br.com.gojopurin.backend.dto.FichaTecnicaResponse;
import br.com.gojopurin.backend.service.FichaTecnicaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Ficha tecnica e custo de um prato (RF-011 a RF-013). So ADMIN e GERENTE.
// Os enderecos comecam com o prato: /api/admin/pratos/{pratoId}/...
@RestController
@RequestMapping("/api/admin/pratos/{pratoId}")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminFichaTecnicaController {

    private final FichaTecnicaService fichaTecnicaService;

    public AdminFichaTecnicaController(FichaTecnicaService fichaTecnicaService) {
        this.fichaTecnicaService = fichaTecnicaService;
    }

    @GetMapping("/ficha")
    public FichaTecnicaResponse buscar(@PathVariable Long pratoId) {
        return fichaTecnicaService.buscar(pratoId);
    }

    // POST e PUT fazem a mesma coisa: salvam a ficha (criam se nao existe,
    // atualizam se existe). O SRS lista os dois metodos, entao os dois funcionam.
    @PostMapping("/ficha")
    public FichaTecnicaResponse criar(@PathVariable Long pratoId, @RequestBody @Valid FichaTecnicaRequest request) {
        return fichaTecnicaService.salvar(pratoId, request);
    }

    @PutMapping("/ficha")
    public FichaTecnicaResponse atualizar(@PathVariable Long pratoId, @RequestBody @Valid FichaTecnicaRequest request) {
        return fichaTecnicaService.salvar(pratoId, request);
    }

    // Faz as contas sem salvar, para a tela mostrar o custo enquanto a pessoa edita.
    @PostMapping("/ficha/simular")
    public FichaTecnicaResponse simular(@PathVariable Long pratoId, @RequestBody @Valid FichaTecnicaRequest request) {
        return fichaTecnicaService.simular(pratoId, request);
    }

    @GetMapping("/custo")
    public CustoResponse custo(@PathVariable Long pratoId) {
        return fichaTecnicaService.custo(pratoId);
    }
}
