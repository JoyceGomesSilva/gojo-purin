package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.IngredienteOpcaoResponse;
import br.com.gojopurin.backend.service.FichaTecnicaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Por enquanto so a lista resumida de ingredientes que a ficha tecnica usa.
// O CRUD completo de ingredientes entra no bloco de estoque (RF-027).
@RestController
@RequestMapping("/api/admin/ingredientes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AdminIngredienteController {

    private final FichaTecnicaService fichaTecnicaService;

    public AdminIngredienteController(FichaTecnicaService fichaTecnicaService) {
        this.fichaTecnicaService = fichaTecnicaService;
    }

    @GetMapping("/opcoes")
    public List<IngredienteOpcaoResponse> opcoes() {
        return fichaTecnicaService.listarIngredientes();
    }
}
