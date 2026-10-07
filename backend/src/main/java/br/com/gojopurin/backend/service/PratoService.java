package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.AdminPratoRequest;
import br.com.gojopurin.backend.dto.AdminPratoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Categoria;
import br.com.gojopurin.backend.model.FichaTecnica;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.repository.CategoriaRepository;
import br.com.gojopurin.backend.repository.FichaTecnicaRepository;
import br.com.gojopurin.backend.repository.PratoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Regras do CRUD de pratos no painel (RF-010).
// O cardapio publico continua no CardapioService.
@Service
public class PratoService {

    private final PratoRepository pratoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;

    public PratoService(PratoRepository pratoRepository,
                        CategoriaRepository categoriaRepository,
                        FichaTecnicaRepository fichaTecnicaRepository) {
        this.pratoRepository = pratoRepository;
        this.categoriaRepository = categoriaRepository;
        this.fichaTecnicaRepository = fichaTecnicaRepository;
    }

    // Lista paginada (RNF08), em ordem alfabetica, com filtros opcionais:
    // categoria, status e um pedaco do nome.
    @Transactional(readOnly = true)
    public PaginaResponse<AdminPratoResponse> listar(Long categoriaId, String status, String busca,
                                                     int pagina, int tamanho) {
        Specification<Prato> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (categoriaId != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (temTexto(status)) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (temTexto(busca)) {
                // LIKE '%ramen%', sem diferenciar maiusculas de minusculas.
                condicoes.add(cb.like(cb.lower(root.<String>get("nome")), "%" + busca.trim().toLowerCase() + "%"));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome"));
        Page<Prato> page = pratoRepository.findAll(filtro, pageable);

        // Busca as fichas dos pratos desta pagina de uma vez so, em vez de
        // fazer uma consulta por prato.
        Map<Long, FichaTecnica> fichas = fichasPorPrato(page.getContent());
        return PaginaResponse.de(page, prato -> AdminPratoResponse.de(prato, fichas.get(prato.getId())));
    }

    @Transactional(readOnly = true)
    public AdminPratoResponse detalhar(Long id) {
        Prato prato = buscar(id);
        return AdminPratoResponse.de(prato, fichaTecnicaRepository.findByPratoId(id).orElse(null));
    }

    @Transactional
    public AdminPratoResponse criar(AdminPratoRequest request) {
        // RN01: prato novo ainda nao tem ficha tecnica, entao nao pode nascer ATIVO.
        if ("ATIVO".equals(request.status())) {
            throw semFicha();
        }

        Prato prato = new Prato();
        preencher(prato, request);
        prato = pratoRepository.save(prato);

        // null = ainda sem ficha tecnica.
        return AdminPratoResponse.de(prato, null);
    }

    @Transactional
    public AdminPratoResponse editar(Long id, AdminPratoRequest request) {
        Prato prato = buscar(id);
        FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(id).orElse(null);

        // RN01: so fica ATIVO se tiver ficha tecnica com pelo menos 1 ingrediente.
        boolean temIngredientes = ficha != null && !ficha.getItens().isEmpty();
        if ("ATIVO".equals(request.status()) && !temIngredientes) {
            throw semFicha();
        }

        preencher(prato, request);
        prato = pratoRepository.save(prato);
        return AdminPratoResponse.de(prato, ficha);
    }

    // Troca so o status, sem mexer no resto (o botao "Ativar" do painel).
    @Transactional
    public AdminPratoResponse mudarStatus(Long id, String status) {
        Prato prato = buscar(id);
        FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(id).orElse(null);

        // RN01 vale aqui tambem.
        boolean temIngredientes = ficha != null && !ficha.getItens().isEmpty();
        if ("ATIVO".equals(status) && !temIngredientes) {
            throw semFicha();
        }

        prato.setStatus(status);
        prato = pratoRepository.save(prato);
        return AdminPratoResponse.de(prato, ficha);
    }

    // RN06: nunca apagamos a linha. O prato fica INATIVO e some do cardapio.
    @Transactional
    public void desativar(Long id) {
        Prato prato = buscar(id);
        prato.setStatus("INATIVO");
        pratoRepository.save(prato);
    }

    // Copia os dados do request para a Entity. Serve para criar e para editar.
    private void preencher(Prato prato, AdminPratoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Categoria não encontrada"));

        prato.setCategoria(categoria);
        prato.setNome(request.nome().trim());
        prato.setDescricao(limpar(request.descricao()));
        prato.setFotoUrl(limpar(request.fotoUrl()));
        prato.setPrecoVenda(request.precoVenda());
        prato.setTempoPreparoMin(request.tempoPreparoMin());
        prato.setAnime(limpar(request.anime()));
        prato.setPersonagem(limpar(request.personagem()));
        prato.setStatus(request.status());
    }

    // Monta um "dicionario": id do prato -> ficha dele.
    private Map<Long, FichaTecnica> fichasPorPrato(List<Prato> pratos) {
        Map<Long, FichaTecnica> fichas = new HashMap<>();
        if (pratos.isEmpty()) {
            return fichas;
        }
        List<Long> ids = pratos.stream().map(Prato::getId).toList();
        for (FichaTecnica ficha : fichaTecnicaRepository.buscarComItensPorPratos(ids)) {
            fichas.put(ficha.getPrato().getId(), ficha);
        }
        return fichas;
    }

    private Prato buscar(Long id) {
        return pratoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prato não encontrado"));
    }

    private ApiException semFicha() {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Para ficar ativo, o prato precisa de ficha técnica com pelo menos 1 ingrediente");
    }

    // Campo opcional: texto vazio vira null no banco.
    private String limpar(String texto) {
        return temTexto(texto) ? texto.trim() : null;
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
