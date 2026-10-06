package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.CategoriaResponse;
import br.com.gojopurin.backend.dto.GrupoOpcaoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PratoCardapioResponse;
import br.com.gojopurin.backend.dto.PratoDetalheResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.repository.CategoriaRepository;
import br.com.gojopurin.backend.repository.PratoGrupoOpcaoRepository;
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
    private final PratoGrupoOpcaoRepository pratoGrupoOpcaoRepository;

    public CardapioService(PratoRepository pratoRepository,
                           CategoriaRepository categoriaRepository,
                           PratoGrupoOpcaoRepository pratoGrupoOpcaoRepository) {
        this.pratoRepository = pratoRepository;
        this.categoriaRepository = categoriaRepository;
        this.pratoGrupoOpcaoRepository = pratoGrupoOpcaoRepository;
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

    // Detalhe do prato, com os grupos de complementos que ele oferece.
    // Grupos que ficaram sem nenhuma opcao ativa nao sao enviados.
    public PratoDetalheResponse detalhar(Long id) {
        Prato prato = pratoRepository.buscarAtivoPorId(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prato não encontrado"));

        List<GrupoOpcaoResponse> grupos = pratoGrupoOpcaoRepository.buscarPorPrato(id).stream()
                .map(ligacao -> GrupoOpcaoResponse.de(ligacao.getGrupoOpcao()))
                .filter(grupo -> !grupo.opcoes().isEmpty())
                .toList();

        return PratoDetalheResponse.de(prato, grupos);
    }
}
