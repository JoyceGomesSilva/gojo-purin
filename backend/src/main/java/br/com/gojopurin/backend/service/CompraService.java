package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.CompraItemRequest;
import br.com.gojopurin.backend.dto.CompraRequest;
import br.com.gojopurin.backend.dto.CompraResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.RecebimentoItemRequest;
import br.com.gojopurin.backend.dto.RecebimentoRequest;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.EstoqueMovimentacao;
import br.com.gojopurin.backend.model.Fornecedor;
import br.com.gojopurin.backend.model.FornecedorProduto;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.model.PedidoCompra;
import br.com.gojopurin.backend.model.PedidoCompraItem;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.EstoqueMovimentacaoRepository;
import br.com.gojopurin.backend.repository.FornecedorProdutoRepository;
import br.com.gojopurin.backend.repository.FornecedorRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import br.com.gojopurin.backend.repository.PedidoCompraRepository;
import br.com.gojopurin.backend.repository.UsuarioRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Pedidos de compra (RF-024) e recebimento (RF-025, RN05).
// Ciclo: RASCUNHO > ENVIADO > RECEBIDO. Tambem pode ser CANCELADO.
@Service
public class CompraService {

    private final PedidoCompraRepository compraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final FornecedorProdutoRepository produtoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final EstoqueMovimentacaoRepository estoqueRepository;
    private final UsuarioRepository usuarioRepository;

    public CompraService(PedidoCompraRepository compraRepository,
                         FornecedorRepository fornecedorRepository,
                         FornecedorProdutoRepository produtoRepository,
                         IngredienteRepository ingredienteRepository,
                         EstoqueMovimentacaoRepository estoqueRepository,
                         UsuarioRepository usuarioRepository) {
        this.compraRepository = compraRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.estoqueRepository = estoqueRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Lista paginada (RNF08), mais recentes primeiro, com filtros opcionais.
    @Transactional(readOnly = true)
    public PaginaResponse<CompraResponse> listar(String status, Long fornecedorId, int pagina, int tamanho) {
        Specification<PedidoCompra> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (fornecedorId != null) {
                condicoes.add(cb.equal(root.get("fornecedor").get("id"), fornecedorId));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return PaginaResponse.de(compraRepository.findAll(filtro, pageable), CompraResponse::de);
    }

    @Transactional(readOnly = true)
    public CompraResponse detalhar(Long id) {
        return CompraResponse.de(buscar(id));
    }

    // Cria o pedido como RASCUNHO.
    @Transactional
    public CompraResponse criar(CompraRequest request, String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);

        PedidoCompra compra = new PedidoCompra();
        compra.setUsuarioId(usuario.getId());
        montarItens(compra, request);
        // saveAndFlush grava na hora, entao os itens ja voltam com o id preenchido.
        return CompraResponse.de(compraRepository.saveAndFlush(compra));
    }

    // Enquanto e RASCUNHO, da para trocar o fornecedor e os itens.
    @Transactional
    public CompraResponse editar(Long id, CompraRequest request) {
        PedidoCompra compra = buscar(id);
        exigirStatus(compra, "RASCUNHO", "Só pedidos em rascunho podem ser alterados");

        montarItens(compra, request);
        return CompraResponse.de(compraRepository.saveAndFlush(compra));
    }

    // RASCUNHO > ENVIADO: o pedido foi mandado ao fornecedor e nao muda mais.
    @Transactional
    public CompraResponse enviar(Long id) {
        PedidoCompra compra = buscar(id);
        exigirStatus(compra, "RASCUNHO", "Só pedidos em rascunho podem ser enviados");

        compra.setStatus("ENVIADO");
        return CompraResponse.de(compraRepository.save(compra));
    }

    // RF-025: ENVIADO > RECEBIDO. Para cada item:
    // - cria uma ENTRADA no estoque com motivo COMPRA, ligada a este pedido;
    // - RN05: o custo unitario do ingrediente passa a ser o preco pago.
    // Como tudo esta no mesmo @Transactional, se algo falhar nada e gravado.
    // O food cost dos pratos se atualiza sozinho, porque a ficha tecnica e
    // sempre calculada com o custo atual do ingrediente.
    @Transactional
    public CompraResponse receber(Long id, RecebimentoRequest request, String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        PedidoCompra compra = buscar(id);
        exigirStatus(compra, "ENVIADO", "Só pedidos enviados podem ser recebidos");

        // Lote e validade informados na tela, por item (opcionais).
        Map<Long, RecebimentoItemRequest> detalhes = new HashMap<>();
        if (request != null && request.itens() != null) {
            for (RecebimentoItemRequest detalhe : request.itens()) {
                detalhes.put(detalhe.itemId(), detalhe);
            }
        }

        for (PedidoCompraItem item : compra.getItens()) {
            Ingrediente ingrediente = item.getIngrediente();
            RecebimentoItemRequest detalhe = detalhes.get(item.getId());

            EstoqueMovimentacao entrada = new EstoqueMovimentacao();
            entrada.setIngredienteId(ingrediente.getId());
            entrada.setTipo("ENTRADA");
            entrada.setMotivo("COMPRA");
            entrada.setQuantidade(item.getQuantidade());
            entrada.setCustoUnitario(item.getPrecoUnitario());
            entrada.setPedidoCompraId(compra.getId());
            entrada.setUsuarioId(usuario.getId());
            if (detalhe != null) {
                entrada.setLote(detalhe.lote() == null || detalhe.lote().isBlank() ? null : detalhe.lote().trim());
                entrada.setValidade(detalhe.validade());
            }
            estoqueRepository.save(entrada);

            // RN05: custo atualiza na compra.
            ingrediente.setCustoUnitario(item.getPrecoUnitario());
            ingredienteRepository.save(ingrediente);
        }

        compra.setStatus("RECEBIDO");
        return CompraResponse.de(compraRepository.save(compra));
    }

    // RN06: "excluir" um pedido de compra e cancelar. So antes de receber:
    // depois disso a mercadoria ja entrou no estoque.
    @Transactional
    public void cancelar(Long id) {
        PedidoCompra compra = buscar(id);
        if (!"RASCUNHO".equals(compra.getStatus()) && !"ENVIADO".equals(compra.getStatus())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Só pedidos em rascunho ou enviados podem ser cancelados");
        }
        compra.setStatus("CANCELADO");
        compraRepository.save(compra);
    }

    // ---------- Internos ----------

    // Monta (ou remonta) os itens a partir do request. O preco de cada item
    // vem do catalogo do fornecedor; o total e calculado aqui (RF-024).
    private void montarItens(PedidoCompra compra, CompraRequest request) {
        Fornecedor fornecedor = fornecedorRepository.findById(request.fornecedorId())
                .filter(encontrado -> "ATIVO".equals(encontrado.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Fornecedor não encontrado ou inativo"));
        compra.setFornecedor(fornecedor);

        // O catalogo deste fornecedor: id do ingrediente -> produto (com o preco).
        Map<Long, FornecedorProduto> catalogo = new HashMap<>();
        for (FornecedorProduto produto : produtoRepository.buscarCatalogo(fornecedor.getId())) {
            catalogo.put(produto.getIngrediente().getId(), produto);
        }

        // Troca os itens antigos pelos novos (orphanRemoval apaga os que sairam).
        compra.getItens().clear();
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> jaVistos = new HashSet<>();

        for (CompraItemRequest pedido : request.itens()) {
            FornecedorProduto produto = catalogo.get(pedido.ingredienteId());
            if (produto == null) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Um dos ingredientes não está no catálogo de " + fornecedor.getRazaoSocial());
            }
            if (!jaVistos.add(pedido.ingredienteId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "O ingrediente " + produto.getIngrediente().getNome() + " aparece mais de uma vez");
            }

            PedidoCompraItem item = new PedidoCompraItem();
            item.setPedidoCompra(compra);
            item.setIngrediente(produto.getIngrediente());
            item.setQuantidade(pedido.quantidade());
            item.setPrecoUnitario(produto.getPreco());
            // subtotal = quantidade x preco, arredondado para centavos
            BigDecimal subtotal = pedido.quantidade().multiply(produto.getPreco()).setScale(2, RoundingMode.HALF_UP);
            item.setSubtotal(subtotal);
            compra.getItens().add(item);

            total = total.add(subtotal);
        }
        compra.setValorTotal(total);
    }

    private void exigirStatus(PedidoCompra compra, String esperado, String mensagem) {
        if (!esperado.equals(compra.getStatus())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
        }
    }

    private PedidoCompra buscar(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido de compra não encontrado"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> "ATIVO".equals(usuario.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Entre de novo."));
    }
}
