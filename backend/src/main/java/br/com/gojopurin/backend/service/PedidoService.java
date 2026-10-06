package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.EnderecoResponse;
import br.com.gojopurin.backend.dto.PaginaResponse;
import br.com.gojopurin.backend.dto.PedidoItemRequest;
import br.com.gojopurin.backend.dto.PedidoRequest;
import br.com.gojopurin.backend.dto.PedidoResponse;
import br.com.gojopurin.backend.dto.PedidoStatusResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Combo;
import br.com.gojopurin.backend.model.ComboEtapa;
import br.com.gojopurin.backend.model.GrupoOpcao;
import br.com.gojopurin.backend.model.Opcao;
import br.com.gojopurin.backend.model.Pedido;
import br.com.gojopurin.backend.model.PedidoItem;
import br.com.gojopurin.backend.model.PedidoItemOpcao;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.model.Usuario;
import br.com.gojopurin.backend.repository.ComboRepository;
import br.com.gojopurin.backend.repository.PedidoRepository;
import br.com.gojopurin.backend.repository.PratoGrupoOpcaoRepository;
import br.com.gojopurin.backend.repository.PratoRepository;
import br.com.gojopurin.backend.repository.UsuarioRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Regras dos pedidos do cliente: criar (checkout), acompanhar e listar.
@Service
public class PedidoService {

    private static final HttpStatus INVALIDO = HttpStatus.UNPROCESSABLE_ENTITY; // 422

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PratoRepository pratoRepository;
    private final PratoGrupoOpcaoRepository pratoGrupoOpcaoRepository;
    private final ComboRepository comboRepository;
    private final EstoqueService estoqueService;

    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioRepository usuarioRepository,
                         PratoRepository pratoRepository,
                         PratoGrupoOpcaoRepository pratoGrupoOpcaoRepository,
                         ComboRepository comboRepository,
                         EstoqueService estoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pratoRepository = pratoRepository;
        this.pratoGrupoOpcaoRepository = pratoGrupoOpcaoRepository;
        this.comboRepository = comboRepository;
        this.estoqueService = estoqueService;
    }

    // ------------------------------------------------------------------
    // Criar pedido (checkout)
    // ------------------------------------------------------------------

    // O front manda so ids e quantidades. Aqui o back:
    // 1. confere se cada prato ainda esta no cardapio;
    // 2. confere os complementos e os combos;
    // 3. calcula todos os precos a partir do banco;
    // 4. confere se ha estoque (RN03), pelo EstoqueService;
    // 5. grava o pedido como RECEBIDO.
    // @Transactional: se qualquer passo falhar, nada e gravado.
    @Transactional
    public PedidoResponse criar(String emailDoCliente, PedidoRequest request) {
        Usuario cliente = buscarUsuario(emailDoCliente);
        Map<String, Combo> combosPorChave = validarCombos(request.itens());

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEnderecoEntrega(request.enderecoEntrega().trim());
        pedido.setObservacoes(textoOuNulo(request.observacoes()));
        pedido.setPago(true); // pagamento simulado (RF-006)

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoItemRequest itemRequest : request.itens()) {
            Prato prato = pratoRepository.buscarAtivoPorId(itemRequest.pratoId())
                    .orElseThrow(() -> new ApiException(INVALIDO,
                            "Um dos pratos do carrinho saiu do cardápio. Remova-o e tente de novo."));

            List<Opcao> opcoes = validarOpcoes(prato, itemRequest.opcaoIds());
            Combo combo = temTexto(itemRequest.comboChave()) ? combosPorChave.get(itemRequest.comboChave()) : null;
            BigDecimal desconto = (combo == null) ? BigDecimal.ZERO : combo.getDescontoPercentual();

            // preco final de uma unidade = prato com desconto + complementos
            BigDecimal precoUnitario = ComboService.aplicarDesconto(prato.getPrecoVenda(), desconto);
            for (Opcao opcao : opcoes) {
                precoUnitario = precoUnitario.add(opcao.getPrecoAdicional());
            }

            PedidoItem item = new PedidoItem();
            item.setPrato(prato);
            item.setQuantidade(itemRequest.quantidade());
            item.setPrecoBase(prato.getPrecoVenda());
            item.setDescontoPercentual(desconto);
            item.setPrecoUnitario(precoUnitario);
            item.setObservacoes(textoOuNulo(itemRequest.observacoes()));
            if (combo != null) {
                item.setComboId(combo.getId());
                item.setComboNome(combo.getNome());
                item.setComboChave(itemRequest.comboChave());
            }
            for (Opcao opcao : opcoes) {
                PedidoItemOpcao escolhida = new PedidoItemOpcao();
                escolhida.setOpcaoId(opcao.getId());
                escolhida.setNome(opcao.getNome());
                escolhida.setPrecoAdicional(opcao.getPrecoAdicional());
                item.adicionarOpcao(escolhida);
            }

            pedido.adicionarItem(item);
            total = total.add(item.getSubtotal());
        }

        // RN03: recusa o pedido se faltar estoque. A baixa de verdade so
        // acontece quando a cozinha confirma (AdminPedidoService).
        estoqueService.conferir(estoqueService.calcularNecessidade(pedido));

        pedido.setValorTotal(total);
        pedido.registrarStatus("RECEBIDO", cliente.getId());

        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    // Confere os complementos marcados para um prato:
    // toda opcao precisa pertencer a um grupo daquele prato, e cada grupo
    // precisa ter entre o minimo e o maximo de escolhas.
    private List<Opcao> validarOpcoes(Prato prato, List<Long> opcaoIds) {
        Set<Long> escolhidas = (opcaoIds == null) ? Set.of() : new HashSet<>(opcaoIds);
        List<Opcao> resultado = new ArrayList<>();

        List<GrupoOpcao> grupos = pratoGrupoOpcaoRepository.buscarPorPrato(prato.getId()).stream()
                .map(ligacao -> ligacao.getGrupoOpcao())
                .toList();

        for (GrupoOpcao grupo : grupos) {
            List<Opcao> doGrupo = grupo.getOpcoes().stream()
                    .filter(opcao -> "ATIVO".equals(opcao.getStatus()))
                    .filter(opcao -> escolhidas.contains(opcao.getId()))
                    .toList();

            if (doGrupo.size() < grupo.getMinEscolhas() || doGrupo.size() > grupo.getMaxEscolhas()) {
                throw new ApiException(INVALIDO,
                        "Revise \"" + grupo.getNome() + "\" em " + prato.getNome() + ".");
            }
            resultado.addAll(doGrupo);
        }

        // Sobrou id que nao pertence a nenhum grupo do prato (ou opcao desativada).
        if (resultado.size() != escolhidas.size()) {
            throw new ApiException(INVALIDO,
                    "Um complemento de " + prato.getNome() + " não está mais disponível. Escolha de novo.");
        }
        return resultado;
    }

    // Confere cada combo do carrinho. As linhas de um mesmo combo chegam com a
    // mesma comboChave. Para o desconto valer:
    // - o combo precisa existir e estar ativo;
    // - cada prato precisa pertencer a uma etapa diferente do combo;
    // - todas as etapas obrigatorias precisam estar preenchidas;
    // - a quantidade de cada prato e 1.
    // Devolve um mapa comboChave -> Combo.
    private Map<String, Combo> validarCombos(List<PedidoItemRequest> itens) {
        // Junta as linhas por chave, mantendo a ordem do carrinho.
        Map<String, List<PedidoItemRequest>> linhasPorChave = new LinkedHashMap<>();
        for (PedidoItemRequest item : itens) {
            if (temTexto(item.comboChave())) {
                linhasPorChave.computeIfAbsent(item.comboChave(), chave -> new ArrayList<>()).add(item);
            } else if (item.comboId() != null) {
                throw comboInvalido();
            }
        }

        Map<String, Combo> combos = new HashMap<>();
        for (Map.Entry<String, List<PedidoItemRequest>> entrada : linhasPorChave.entrySet()) {
            List<PedidoItemRequest> linhas = entrada.getValue();
            Long comboId = linhas.get(0).comboId();
            if (comboId == null) {
                throw comboInvalido();
            }
            Combo combo = comboRepository.findByIdAndStatus(comboId, "ATIVO")
                    .orElseThrow(() -> new ApiException(INVALIDO,
                            "Um combo do carrinho não está mais disponível. Remova-o e tente de novo."));

            Set<Long> etapasUsadas = new HashSet<>();
            for (PedidoItemRequest linha : linhas) {
                if (!comboId.equals(linha.comboId()) || linha.quantidade() == null || linha.quantidade() != 1) {
                    throw comboInvalido();
                }
                // Procura uma etapa ainda livre que aceite este prato.
                ComboEtapa etapa = combo.getEtapas().stream()
                        .filter(e -> !etapasUsadas.contains(e.getId()))
                        .filter(e -> e.getPratos().stream().anyMatch(p -> p.getId().equals(linha.pratoId())))
                        .findFirst()
                        .orElseThrow(this::comboInvalido);
                etapasUsadas.add(etapa.getId());
            }

            boolean faltaObrigatoria = combo.getEtapas().stream()
                    .anyMatch(e -> e.isObrigatoria() && !etapasUsadas.contains(e.getId()));
            if (faltaObrigatoria) {
                throw comboInvalido();
            }
            combos.put(entrada.getKey(), combo);
        }
        return combos;
    }

    private ApiException comboInvalido() {
        return new ApiException(INVALIDO,
                "Um combo do carrinho está incompleto. Remova-o, monte de novo e tente outra vez.");
    }

    // ------------------------------------------------------------------
    // Consultas do cliente
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PaginaResponse<PedidoResponse> listarDoCliente(String emailDoCliente, Pageable pageable) {
        Usuario cliente = buscarUsuario(emailDoCliente);
        return PaginaResponse.de(
                pedidoRepository.findByClienteIdOrderByCreatedAtDesc(cliente.getId(), pageable),
                PedidoResponse::de);
    }

    @Transactional(readOnly = true)
    public PedidoResponse detalhar(String emailDoCliente, Long pedidoId) {
        return PedidoResponse.de(buscarDoCliente(emailDoCliente, pedidoId));
    }

    @Transactional(readOnly = true)
    public PedidoStatusResponse status(String emailDoCliente, Long pedidoId) {
        return PedidoStatusResponse.de(buscarDoCliente(emailDoCliente, pedidoId));
    }

    @Transactional(readOnly = true)
    public EnderecoResponse enderecoCadastrado(String emailDoCliente) {
        return new EnderecoResponse(buscarUsuario(emailDoCliente).getEndereco());
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    // So acha o pedido se ele for do cliente logado. Para pedido de outra
    // pessoa a resposta e a mesma de pedido inexistente: 404.
    private Pedido buscarDoCliente(String emailDoCliente, Long pedidoId) {
        Usuario cliente = buscarUsuario(emailDoCliente);
        return pedidoRepository.findByIdAndClienteId(pedidoId, cliente.getId())
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

    private static String textoOuNulo(String texto) {
        return temTexto(texto) ? texto.trim() : null;
    }
}
