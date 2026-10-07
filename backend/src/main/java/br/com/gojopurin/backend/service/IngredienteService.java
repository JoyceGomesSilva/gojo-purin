package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.IngredienteRequest;
import br.com.gojopurin.backend.dto.IngredienteResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.repository.FichaTecnicaRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Regras do CRUD de ingredientes (RF-027).
@Service
public class IngredienteService {

    private final IngredienteRepository ingredienteRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;

    public IngredienteService(IngredienteRepository ingredienteRepository,
                              FichaTecnicaRepository fichaTecnicaRepository) {
        this.ingredienteRepository = ingredienteRepository;
        this.fichaTecnicaRepository = fichaTecnicaRepository;
    }

    // Lista paginada (RNF08), em ordem alfabetica. Filtros opcionais: status e
    // um pedaco do nome ou do SKU.
    @Transactional(readOnly = true)
    public PaginaResponse<IngredienteResponse> listar(String status, String busca, int pagina, int tamanho) {
        Specification<Ingrediente> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (temTexto(status)) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (temTexto(busca)) {
                String trecho = "%" + busca.trim().toLowerCase() + "%";
                // nome LIKE '%...%' OR sku LIKE '%...%'
                condicoes.add(cb.or(
                        cb.like(cb.lower(root.<String>get("nome")), trecho),
                        cb.like(cb.lower(root.<String>get("sku")), trecho)));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("nome"));
        return PaginaResponse.de(ingredienteRepository.findAll(filtro, pageable), IngredienteResponse::de);
    }

    @Transactional(readOnly = true)
    public IngredienteResponse detalhar(Long id) {
        return IngredienteResponse.de(buscar(id));
    }

    @Transactional
    public IngredienteResponse criar(IngredienteRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (ingredienteRepository.existsBySkuIgnoreCase(sku)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um ingrediente com este SKU");
        }

        Ingrediente ingrediente = new Ingrediente();
        // A unidade so e definida na criacao.
        ingrediente.setUnidadePadrao(request.unidadePadrao());
        preencher(ingrediente, request, sku);
        return IngredienteResponse.de(ingredienteRepository.save(ingrediente));
    }

    @Transactional
    public IngredienteResponse editar(Long id, IngredienteRequest request) {
        Ingrediente ingrediente = buscar(id);
        String sku = request.sku().trim().toUpperCase();
        if (ingredienteRepository.existsBySkuIgnoreCaseAndIdNot(sku, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um ingrediente com este SKU");
        }

        // O saldo, as fichas tecnicas e o custo estao todos nesta unidade.
        // Trocar "g" por "kg" faria 500 g virarem 500 kg, entao nao e permitido.
        if (!ingrediente.getUnidadePadrao().equals(request.unidadePadrao())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A unidade não pode ser alterada depois que o ingrediente é criado");
        }

        if ("INATIVO".equals(request.status())) {
            conferirSePodeDesativar(ingrediente);
        }

        preencher(ingrediente, request, sku);
        return IngredienteResponse.de(ingredienteRepository.save(ingrediente));
    }

    // RN06: nunca apagamos a linha. O ingrediente fica INATIVO.
    @Transactional
    public void desativar(Long id) {
        Ingrediente ingrediente = buscar(id);
        conferirSePodeDesativar(ingrediente);
        ingrediente.setStatus("INATIVO");
        ingredienteRepository.save(ingrediente);
    }

    // Um ingrediente usado em prato ATIVO nao pode ser desativado: o prato
    // continuaria a venda com a receita incompleta.
    private void conferirSePodeDesativar(Ingrediente ingrediente) {
        if ("INATIVO".equals(ingrediente.getStatus())) {
            return; // ja estava inativo, nada a conferir
        }
        List<String> pratos = fichaTecnicaRepository.nomesDosPratosAtivosQueUsam(ingrediente.getId());
        if (!pratos.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este ingrediente está na ficha técnica de pratos ativos. "
                            + "Tire-o das fichas ou desative os pratos antes.",
                    pratos);
        }
    }

    // Copia os dados do request para a Entity. Serve para criar e para editar.
    private void preencher(Ingrediente ingrediente, IngredienteRequest request, String sku) {
        ingrediente.setNome(request.nome().trim());
        ingrediente.setSku(sku);
        ingrediente.setEstoqueMinimo(request.estoqueMinimo());
        ingrediente.setCustoUnitario(request.custoUnitario());
        ingrediente.setStatus(request.status());
    }

    private Ingrediente buscar(Long id) {
        return ingredienteRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ingrediente não encontrado"));
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
