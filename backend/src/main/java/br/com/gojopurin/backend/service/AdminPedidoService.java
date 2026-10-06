package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.AdminPedidoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Pedido;
import br.com.gojopurin.backend.model.Perfil;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.PedidoRepository;
import br.com.gojopurin.backend.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Regras dos pedidos no painel da cozinha: listar, detalhar, avancar o
// status e cancelar.
@Service
public class AdminPedidoService {

    // A ordem do ciclo (RF-016). Um pedido so anda para o passo seguinte.
    private static final List<String> CICLO =
            List.of("RECEBIDO", "CONFIRMADO", "EM_PREPARO", "PRONTO", "SAIU_ENTREGA", "FINALIZADO");

    // O cozinheiro so faz estas duas mudancas (secao 1.3 do SRS).
    private static final List<String> STATUS_DO_COZINHEIRO = List.of("EM_PREPARO", "PRONTO");

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueService estoqueService;

    public AdminPedidoService(PedidoRepository pedidoRepository,
                              UsuarioRepository usuarioRepository,
                              EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estoqueService = estoqueService;
    }

    // RF-019: lista paginada, mais recentes primeiro, com filtros opcionais.
    // Cada filtro so entra na consulta se foi informado.
    @Transactional(readOnly = true)
    public PaginaResponse<AdminPedidoResponse> listar(String status, String canal, LocalDate de, LocalDate ate,
                                                      int pagina, int tamanho) {
        Specification<Pedido> filtro = (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (temTexto(status)) {
                condicoes.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (temTexto(canal)) {
                condicoes.add(cb.equal(root.get("canal"), canal.trim().toUpperCase()));
            }
            if (de != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("createdAt"), de.atStartOfDay()));
            }
            if (ate != null) {
                // "ate o dia X" inclui o dia X inteiro.
                condicoes.add(cb.lessThan(root.get("createdAt"), ate.plusDays(1).atStartOfDay()));
            }
            return cb.and(condicoes.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PaginaResponse.de(pedidoRepository.findAll(filtro, pageable), AdminPedidoResponse::de);
    }

    @Transactional(readOnly = true)
    public AdminPedidoResponse detalhar(Long id) {
        return AdminPedidoResponse.de(buscar(id));
    }

    // RF-016: avanca o pedido para o proximo status do ciclo.
    // RF-017: ao chegar em CONFIRMADO, da a baixa no estoque. Como tudo esta
    // no mesmo @Transactional, se faltar ingrediente nada e gravado: nem a
    // baixa, nem a troca de status.
    @Transactional
    public AdminPedidoResponse mudarStatus(Long id, String novoStatus, String emailDoUsuario) {
        Usuario usuario = buscarUsuario(emailDoUsuario);
        Pedido pedido = buscar(id);
        String destino = novoStatus.trim().toUpperCase();

        int posicaoAtual = CICLO.indexOf(pedido.getStatus());
        int posicaoDestino = CICLO.indexOf(destino);
        if (posicaoDestino < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Status desconhecido: " + novoStatus);
        }
        if (posicaoAtual < 0 || posicaoDestino != posicaoAtual + 1) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "O pedido está em " + pedido.getStatus() + " e não pode ir para " + destino + ".");
        }
        if (usuario.getPerfil() == Perfil.COZINHEIRO && !STATUS_DO_COZINHEIRO.contains(destino)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seu perfil não pode fazer esta mudança de status.");
        }

        if ("CONFIRMADO".equals(destino)) {
            estoqueService.baixarPorPedido(pedido, usuario.getId());
        }
        pedido.registrarStatus(destino, usuario.getId());
        return AdminPedidoResponse.de(pedidoRepository.save(pedido));
    }

    // RF-018 e RN04: cancela com motivo e devolve ao estoque o que tinha sido baixado.
    // Antes de EM_PREPARO qualquer pessoa da equipe cancela; a partir dai,
    // so GERENTE ou ADMIN.
    @Transactional
    public AdminPedidoResponse cancelar(Long id, String motivo, String emailDoUsuario) {
        Usuario usuario = buscarUsuario(emailDoUsuario);
        Pedido pedido = buscar(id);

        if ("CANCELADO".equals(pedido.getStatus()) || "FINALIZADO".equals(pedido.getStatus())) {
            throw new ApiException(HttpStatus.CONFLICT, "Este pedido já foi encerrado e não pode ser cancelado.");
        }
        boolean jaEmPreparo = CICLO.indexOf(pedido.getStatus()) >= CICLO.indexOf("EM_PREPARO");
        if (jaEmPreparo && usuario.getPerfil() == Perfil.COZINHEIRO) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Depois que o preparo começa, só gerente ou admin pode cancelar.");
        }

        estoqueService.estornarPedido(pedido, usuario.getId());
        pedido.setMotivoCancelamento(motivo.trim());
        pedido.registrarStatus("CANCELADO", usuario.getId());
        return AdminPedidoResponse.de(pedidoRepository.save(pedido));
    }

    private Pedido buscar(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> "ATIVO".equals(usuario.getStatus()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Entre de novo."));
    }

    private static boolean temTexto(String texto) {
        return texto != null && !texto.isBlank();
    }
}
