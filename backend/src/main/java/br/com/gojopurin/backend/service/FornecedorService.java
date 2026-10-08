package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.CatalogoItemRequest;
import br.com.gojopurin.backend.dto.CatalogoItemResponse;
import br.com.gojopurin.backend.dto.CotacaoOfertaResponse;
import br.com.gojopurin.backend.dto.CotacaoResponse;
import br.com.gojopurin.backend.dto.FornecedorRequest;
import br.com.gojopurin.backend.dto.FornecedorResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PrecoHistoricoResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Fornecedor;
import br.com.gojopurin.backend.model.FornecedorPrecoHistorico;
import br.com.gojopurin.backend.model.FornecedorProduto;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.repository.FornecedorPrecoHistoricoRepository;
import br.com.gojopurin.backend.repository.FornecedorProdutoRepository;
import br.com.gojopurin.backend.repository.FornecedorRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import br.com.gojopurin.backend.validation.CnpjValidator;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fornecedores (RF-021), catalogo (RF-022), cotacao comparativa (RF-023)
// e historico de precos (RF-026).
@Service
public class FornecedorService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final FornecedorRepository fornecedorRepository;
    private final FornecedorProdutoRepository produtoRepository;
    private final FornecedorPrecoHistoricoRepository historicoRepository;
    private final IngredienteRepository ingredienteRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository,
                             FornecedorProdutoRepository produtoRepository,
                             FornecedorPrecoHistoricoRepository historicoRepository,
                             IngredienteRepository ingredienteRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
        this.historicoRepository = historicoRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    // ---------- CRUD (RF-021) ----------

    // Lista paginada (RNF08), em ordem alfabetica. Filtros opcionais: status e
    // um pedaco da razao social ou do CNPJ.
    @Transactional(readOnly = true)
    public PaginaResponse<FornecedorResponse> listar(String status, String busca, int pagina, int tamanho) {
        Specification<Fornecedor> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (temTexto(status)) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (temTexto(busca)) {
                String trecho = "%" + busca.trim().toLowerCase() + "%";
                // Para buscar pelo CNPJ, tira a pontuacao do que foi digitado.
                String cnpj = "%" + CnpjValidator.normalizar(busca) + "%";
                condicoes.add(cb.or(
                        cb.like(cb.lower(root.<String>get("razaoSocial")), trecho),
                        cb.like(root.<String>get("cnpj"), cnpj)));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("razaoSocial"));
        return PaginaResponse.de(fornecedorRepository.findAll(filtro, pageable), FornecedorResponse::de);
    }

    // Os ativos, para os campos de escolha (tela de compras).
    @Transactional(readOnly = true)
    public List<FornecedorResponse> listarAtivos() {
        return fornecedorRepository.findByStatusOrderByRazaoSocialAsc("ATIVO").stream()
                .map(FornecedorResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponse detalhar(Long id) {
        return FornecedorResponse.de(buscar(id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        String cnpj = CnpjValidator.normalizar(request.cnpj());
        if (fornecedorRepository.existsByCnpj(cnpj)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um fornecedor com este CNPJ");
        }

        Fornecedor fornecedor = new Fornecedor();
        preencher(fornecedor, request, cnpj);
        return FornecedorResponse.de(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponse editar(Long id, FornecedorRequest request) {
        Fornecedor fornecedor = buscar(id);
        String cnpj = CnpjValidator.normalizar(request.cnpj());
        if (fornecedorRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um fornecedor com este CNPJ");
        }

        preencher(fornecedor, request, cnpj);
        return FornecedorResponse.de(fornecedorRepository.save(fornecedor));
    }

    // RN06: nunca apagamos a linha. O fornecedor fica INATIVO e sai das
    // cotacoes e dos novos pedidos de compra.
    @Transactional
    public void desativar(Long id) {
        Fornecedor fornecedor = buscar(id);
        fornecedor.setStatus("INATIVO");
        fornecedorRepository.save(fornecedor);
    }

    // ---------- Catalogo (RF-022) ----------

    @Transactional(readOnly = true)
    public List<CatalogoItemResponse> listarCatalogo(Long fornecedorId) {
        buscar(fornecedorId); // 404 se o fornecedor nao existe
        return produtoRepository.buscarCatalogo(fornecedorId).stream()
                .map(CatalogoItemResponse::de)
                .toList();
    }

    // Coloca um ingrediente no catalogo ou atualiza o preco dele.
    // Toda vez que o preco e novo, grava um ponto no historico (RF-026).
    @Transactional
    public CatalogoItemResponse salvarNoCatalogo(Long fornecedorId, CatalogoItemRequest request) {
        Fornecedor fornecedor = buscar(fornecedorId);
        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Ingrediente não encontrado"));

        FornecedorProduto produto = produtoRepository
                .findByFornecedorIdAndIngredienteId(fornecedorId, ingrediente.getId())
                .orElse(null);

        // compareTo == 0 compara so o valor: 0.0120 e 0.012 contam como iguais.
        boolean precoMudou = produto == null || produto.getPreco().compareTo(request.preco()) != 0;

        if (produto == null) {
            produto = new FornecedorProduto();
            produto.setFornecedor(fornecedor);
            produto.setIngrediente(ingrediente);
            // A unidade de venda e a unidade padrao do ingrediente: e nela que o
            // estoque e o custo sao contados, entao nao ha conversao a fazer.
            produto.setUnidadeVenda(ingrediente.getUnidadePadrao());
        }
        produto.setPreco(request.preco());
        produto = produtoRepository.save(produto);

        if (precoMudou) {
            FornecedorPrecoHistorico ponto = new FornecedorPrecoHistorico();
            ponto.setFornecedorId(fornecedorId);
            ponto.setIngredienteId(ingrediente.getId());
            ponto.setPreco(request.preco());
            historicoRepository.save(ponto);
        }

        return CatalogoItemResponse.de(produto);
    }

    // ---------- Cotacao (RF-023) e historico (RF-026) ----------

    @Transactional(readOnly = true)
    public CotacaoResponse cotar(Long ingredienteId) {
        Ingrediente ingrediente = ingredienteRepository.findById(ingredienteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ingrediente não encontrado"));

        // Ja vem do mais barato para o mais caro (order by no repository).
        List<FornecedorProduto> produtos = produtoRepository.buscarCotacao(ingredienteId);
        BigDecimal menorPreco = produtos.isEmpty() ? null : produtos.get(0).getPreco();

        List<CotacaoOfertaResponse> ofertas = new ArrayList<>();
        for (FornecedorProduto produto : produtos) {
            Fornecedor fornecedor = produto.getFornecedor();
            // (preco / menor preco - 1) x 100 = quantos % mais caro que o mais barato
            BigDecimal diferenca = produto.getPreco()
                    .divide(menorPreco, 6, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE)
                    .multiply(CEM)
                    .setScale(1, RoundingMode.HALF_UP);
            ofertas.add(new CotacaoOfertaResponse(
                    fornecedor.getId(),
                    fornecedor.getRazaoSocial(),
                    fornecedor.getTelefone(),
                    fornecedor.getEmail(),
                    produto.getPreco(),
                    produto.getPreco().compareTo(menorPreco) == 0,
                    diferenca));
        }

        // Historico de todos os fornecedores que ja venderam este ingrediente.
        List<FornecedorPrecoHistorico> pontos = historicoRepository.findByIngredienteIdOrderByCreatedAtAscIdAsc(ingredienteId);
        Map<Long, String> nomes = new HashMap<>();
        for (Fornecedor fornecedor : fornecedorRepository.findAllById(
                pontos.stream().map(FornecedorPrecoHistorico::getFornecedorId).distinct().toList())) {
            nomes.put(fornecedor.getId(), fornecedor.getRazaoSocial());
        }
        List<PrecoHistoricoResponse> historico = pontos.stream()
                .map(ponto -> new PrecoHistoricoResponse(
                        ponto.getFornecedorId(),
                        nomes.getOrDefault(ponto.getFornecedorId(), "Fornecedor " + ponto.getFornecedorId()),
                        ponto.getPreco(),
                        ponto.getCreatedAt()))
                .toList();

        return new CotacaoResponse(
                ingrediente.getId(),
                ingrediente.getNome(),
                ingrediente.getUnidadePadrao(),
                ingrediente.getCustoUnitario(),
                ofertas,
                historico);
    }

    // ---------- Internos ----------

    private void preencher(Fornecedor fornecedor, FornecedorRequest request, String cnpj) {
        fornecedor.setRazaoSocial(request.razaoSocial().trim());
        fornecedor.setCnpj(cnpj);
        fornecedor.setTelefone(limpar(request.telefone()));
        fornecedor.setEmail(limpar(request.email()) == null ? null : request.email().trim().toLowerCase());
        fornecedor.setCategoriasProdutos(limpar(request.categoriasProdutos()));
        fornecedor.setStatus(request.status());
    }

    private Fornecedor buscar(Long id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Fornecedor não encontrado"));
    }

    private String limpar(String texto) {
        return temTexto(texto) ? texto.trim() : null;
    }

    private boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
