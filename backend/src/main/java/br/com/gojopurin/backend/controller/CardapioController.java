package br.com.gojopurin.backend.controller;

import br.com.gojopurin.backend.dto.CategoriaResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PratoCardapioResponse;
import br.com.gojopurin.backend.service.CardapioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Cardapio publico: nao exige login (liberado no SecurityConfig).
@RestController
@RequestMapping("/api/cardapio")
public class CardapioController {

    private static final int TAMANHO_MAXIMO = 50;

    private final CardapioService cardapioService;

    public CardapioController(CardapioService cardapioService) {
        this.cardapioService = cardapioService;
    }

    // GET /api/cardapio?categoriaId=3&page=0&size=12
    @GetMapping
    public PaginaResponse<PratoCardapioResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        // Limita o tamanho da pagina para ninguem pedir 10.000 registros de uma vez.
        int tamanho = Math.max(1, Math.min(size, TAMANHO_MAXIMO));
        Pageable pageable = PageRequest.of(Math.max(page, 0), tamanho);
        return cardapioService.listarPratos(categoriaId, pageable);
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return cardapioService.listarCategorias();
    }

    @GetMapping("/{id}")
    public PratoCardapioResponse detalhar(@PathVariable Long id) {
        return cardapioService.detalhar(id);
    }
}
