package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.CategoriaResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PratoCardapioResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.repository.CategoriaRepository;
import br.com.gojopurin.backend.repository.PratoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Regras do cardapio publico: so mostra o que esta ATIVO (RN09).
@Service
@Transactional(readOnly = true)
public class CardapioService {

    private final PratoRepository pratoRepository;
    private final CategoriaRepository categoriaRepository;

    public CardapioService(PratoRepository pratoRepository, CategoriaRepository categoriaRepository) {
        this.pratoRepository = pratoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findByStatusOrderByOrdemAsc("ATIVO").stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    // categoriaId e opcional: sem ele, traz o cardapio inteiro.
    public PaginaResponse<PratoCardapioResponse> listarPratos(Long categoriaId, Pageable pageable) {
        Page<Prato> pagina = (categoriaId == null)
                ? pratoRepository.buscarAtivos(pageable)
                : pratoRepository.buscarAtivosPorCategoria(categoriaId, pageable);
        return PaginaResponse.de(pagina, PratoCardapioResponse::de);
    }

    public PratoCardapioResponse detalhar(Long id) {
        return pratoRepository.buscarAtivoPorId(id)
                .map(PratoCardapioResponse::de)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prato não encontrado"));
    }
}
