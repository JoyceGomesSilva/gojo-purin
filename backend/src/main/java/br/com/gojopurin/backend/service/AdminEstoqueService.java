package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.MovimentacaoRequest;
import br.com.gojopurin.backend.dto.MovimentacaoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.SaldoResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.EstoqueMovimentacao;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.model.MotivoPerda;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.EstoqueMovimentacaoRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import br.com.gojopurin.backend.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// O estoque visto pelo painel: saldo (RF-031), alertas (RF-032), entrada e
// saida manuais (RF-028 e RF-030) e o historico (RF-033).
// A baixa e o estorno automaticos dos pedidos continuam no EstoqueService.
@Service
public class AdminEstoqueService {

    private final EstoqueMovimentacaoRepository estoqueRepository;
    private final IngredienteRepository ingredienteRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminEstoqueService(EstoqueMovimentacaoRepository estoqueRepository,
                               IngredienteRepository ingredienteRepository,
                               UsuarioRepository usuarioRepository) {
        this.estoqueRepository = estoqueRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // RF-031: o saldo de cada ingrediente ativo, em ordem alfabetica.
    // O saldo nao fica gravado em lugar nenhum: e sempre a soma das
    // movimentacoes (entradas e estornos somam, saidas subtraem).
    @Transactional(readOnly = true)
    public List<SaldoResponse> listarSaldos() {
        Map<Long, BigDecimal> saldos = new HashMap<>();
        for (Object[] linha : estoqueRepository.somarTodosOsSaldos()) {
            saldos.put(((Number) linha[0]).longValue(), new BigDecimal(linha[1].toString()));
        }

        return ingredienteRepository.findByStatusOrderByNomeAsc("ATIVO").stream()
                // Ingrediente que nunca teve movimentacao tem saldo zero.
                .map(ingrediente -> SaldoResponse.de(ingrediente, saldos.getOrDefault(ingrediente.getId(), BigDecimal.ZERO)))
                .toList();
    }

    // RF-032: so os que estao abaixo do estoque minimo.
    @Transactional(readOnly = true)
    public List<SaldoResponse> listarAlertas() {
        return listarSaldos().stream()
                .filter(SaldoResponse::abaixoDoMinimo)
                .toList();
    }

    // Entrada ou saida manual. Devolve o saldo novo do ingrediente.
    @Transactional
    public SaldoResponse movimentar(MovimentacaoRequest request, String emailUsuario) {
        Usuario usuario = buscarUsuario(emailUsuario);
        Ingrediente ingrediente = ingredienteRepository.findById(request.ingredienteId())
                .filter(encontrado -> "ATIVO".equals(encontrado.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Ingrediente não encontrado ou inativo"));

        BigDecimal saldoAtual = saldoDe(ingrediente.getId());

        EstoqueMovimentacao movimentacao = new EstoqueMovimentacao();
        movimentacao.setIngredienteId(ingrediente.getId());
        movimentacao.setTipo(request.tipo());
        movimentacao.setQuantidade(request.quantidade());
        movimentacao.setUsuarioId(usuario.getId());

        BigDecimal saldoNovo;
        if ("ENTRADA".equals(request.tipo())) {
            // RF-028: entrada manual e sempre um AJUSTE (a de COMPRA nasce do
            // recebimento do pedido de compra).
            movimentacao.setMotivo("AJUSTE");
            movimentacao.setLote(limpar(request.lote()));
            movimentacao.setValidade(request.validade());
            // Sem custo informado, vale o custo atual do ingrediente.
            movimentacao.setCustoUnitario(
                    request.custoUnitario() != null ? request.custoUnitario() : ingrediente.getCustoUnitario());
            saldoNovo = saldoAtual.add(request.quantidade());
        } else {
            // RF-030: saida manual exige um motivo da lista fechada.
            movimentacao.setMotivo(paraMotivoPerda(request.motivo()).name());
            movimentacao.setCustoUnitario(ingrediente.getCustoUnitario());

            // Nao da para tirar mais do que existe.
            if (saldoAtual.compareTo(request.quantidade()) < 0) {
                String unidade = ingrediente.getUnidadePadrao().toLowerCase();
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Saldo insuficiente: há " + numero(saldoAtual.max(BigDecimal.ZERO)) + " " + unidade
                                + " de " + ingrediente.getNome());
            }
            saldoNovo = saldoAtual.subtract(request.quantidade());
        }

        estoqueRepository.save(movimentacao);
        return SaldoResponse.de(ingrediente, saldoNovo);
    }

    // RF-033: historico paginado, do mais recente para o mais antigo, com
    // filtros opcionais por ingrediente e por tipo.
    @Transactional(readOnly = true)
    public PaginaResponse<MovimentacaoResponse> listarMovimentacoes(Long ingredienteId, String tipo,
                                                                    int pagina, int tamanho) {
        Specification<EstoqueMovimentacao> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (ingredienteId != null) {
                condicoes.add(cb.equal(root.get("ingredienteId"), ingredienteId));
            }
            if (tipo != null && !tipo.isBlank()) {
                condicoes.add(cb.equal(root.get("tipo"), tipo.trim().toUpperCase()));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        // Desempate pelo id: movimentacoes gravadas no mesmo instante mantem a ordem.
        Pageable pageable = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<EstoqueMovimentacao> page = estoqueRepository.findAll(filtro, pageable);

        // A movimentacao guarda so os ids. Busca os nomes dos ingredientes e
        // dos usuarios desta pagina de uma vez, em vez de uma consulta por linha.
        Set<Long> ingredienteIds = new HashSet<>();
        Set<Long> usuarioIds = new HashSet<>();
        for (EstoqueMovimentacao movimentacao : page.getContent()) {
            ingredienteIds.add(movimentacao.getIngredienteId());
            usuarioIds.add(movimentacao.getUsuarioId());
        }
        Map<Long, Ingrediente> ingredientes = new HashMap<>();
        for (Ingrediente ingrediente : ingredienteRepository.findAllById(ingredienteIds)) {
            ingredientes.put(ingrediente.getId(), ingrediente);
        }
        Map<Long, String> nomesDosUsuarios = new HashMap<>();
        for (Usuario usuario : usuarioRepository.findAllById(usuarioIds)) {
            nomesDosUsuarios.put(usuario.getId(), usuario.getNome());
        }

        return PaginaResponse.de(page, movimentacao -> {
            Ingrediente ingrediente = ingredientes.get(movimentacao.getIngredienteId());
            return new MovimentacaoResponse(
                    movimentacao.getId(),
                    movimentacao.getCreatedAt(),
                    movimentacao.getIngredienteId(),
                    ingrediente == null ? "Ingrediente " + movimentacao.getIngredienteId() : ingrediente.getNome(),
                    ingrediente == null ? "" : ingrediente.getUnidadePadrao(),
                    movimentacao.getTipo(),
                    movimentacao.getMotivo(),
                    movimentacao.getQuantidade(),
                    movimentacao.getLote(),
                    movimentacao.getValidade(),
                    movimentacao.getCustoUnitario(),
                    nomesDosUsuarios.getOrDefault(movimentacao.getUsuarioId(), ""),
                    movimentacao.getPedidoId(),
                    movimentacao.getPedidoCompraId());
        });
    }

    // ---------- Internos ----------

    private BigDecimal saldoDe(Long ingredienteId) {
        for (Object[] linha : estoqueRepository.somarSaldos(List.of(ingredienteId))) {
            return new BigDecimal(linha[1].toString());
        }
        return BigDecimal.ZERO; // nenhuma movimentacao ainda
    }

    // Transforma o texto "QUEBRA" no valor MotivoPerda.QUEBRA.
    // valueOf lanca erro se o texto nao for um dos valores do enum.
    private MotivoPerda paraMotivoPerda(String texto) {
        try {
            return MotivoPerda.valueOf(texto == null ? "" : texto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Informe o motivo da saída: DESPERDICIO, VENCIMENTO, QUEBRA ou USO_INTERNO");
        }
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> "ATIVO".equals(usuario.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Entre de novo."));
    }

    private String limpar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    // 80.000 vira "80"; 12.500 vira "12.5".
    private static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
