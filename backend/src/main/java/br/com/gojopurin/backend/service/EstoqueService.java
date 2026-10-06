package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.EstoqueMovimentacao;
import br.com.gojopurin.backend.model.FichaTecnica;
import br.com.gojopurin.backend.model.FichaTecnicaItem;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.model.Opcao;
import br.com.gojopurin.backend.model.Pedido;
import br.com.gojopurin.backend.model.PedidoItem;
import br.com.gojopurin.backend.model.PedidoItemOpcao;
import br.com.gojopurin.backend.repository.EstoqueMovimentacaoRepository;
import br.com.gojopurin.backend.repository.FichaTecnicaRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import br.com.gojopurin.backend.repository.OpcaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Tudo o que liga pedido e estoque:
// - calcular o que um pedido gasta;
// - conferir se ha saldo (RN03);
// - dar a baixa quando o pedido e confirmado (RF-017);
// - devolver ao estoque quando o pedido e cancelado (RF-018).
@Service
public class EstoqueService {

    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final IngredienteRepository ingredienteRepository;
    private final OpcaoRepository opcaoRepository;
    private final EstoqueMovimentacaoRepository estoqueRepository;

    public EstoqueService(FichaTecnicaRepository fichaTecnicaRepository,
                          IngredienteRepository ingredienteRepository,
                          OpcaoRepository opcaoRepository,
                          EstoqueMovimentacaoRepository estoqueRepository) {
        this.fichaTecnicaRepository = fichaTecnicaRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.opcaoRepository = opcaoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    // Quanto de cada ingrediente o pedido gasta (id do ingrediente -> quantidade).
    // Pela ficha tecnica: quantidade x fator de correcao x porcoes / rendimento.
    // Pelos adicionais: quantidade do adicional x porcoes (ex.: "Ovo extra" = 1 ovo).
    public Map<Long, BigDecimal> calcularNecessidade(Pedido pedido) {
        Map<Long, BigDecimal> necessidade = new LinkedHashMap<>();

        Set<Long> pratoIds = new HashSet<>();
        Set<Long> opcaoIds = new HashSet<>();
        for (PedidoItem item : pedido.getItens()) {
            pratoIds.add(item.getPrato().getId());
            for (PedidoItemOpcao escolhida : item.getOpcoes()) {
                opcaoIds.add(escolhida.getOpcaoId());
            }
        }

        Map<Long, FichaTecnica> fichaPorPrato = new HashMap<>();
        for (FichaTecnica ficha : fichaTecnicaRepository.buscarPorPratos(pratoIds)) {
            fichaPorPrato.put(ficha.getPrato().getId(), ficha);
        }
        Map<Long, Opcao> opcaoPorId = new HashMap<>();
        for (Opcao opcao : opcaoRepository.findAllById(opcaoIds)) {
            opcaoPorId.put(opcao.getId(), opcao);
        }

        for (PedidoItem item : pedido.getItens()) {
            BigDecimal porcoes = BigDecimal.valueOf(item.getQuantidade());

            FichaTecnica ficha = fichaPorPrato.get(item.getPrato().getId());
            if (ficha != null) {
                BigDecimal rendimento = BigDecimal.valueOf(ficha.getRendimento());
                for (FichaTecnicaItem linha : ficha.getItens()) {
                    BigDecimal gasto = linha.getQuantidade()
                            .multiply(linha.getFatorCorrecao())
                            .multiply(porcoes)
                            .divide(rendimento, 3, RoundingMode.HALF_UP);
                    necessidade.merge(linha.getIngrediente().getId(), gasto, BigDecimal::add);
                }
            }

            for (PedidoItemOpcao escolhida : item.getOpcoes()) {
                Opcao opcao = opcaoPorId.get(escolhida.getOpcaoId());
                if (opcao != null && opcao.getIngredienteId() != null && opcao.getQuantidadeIngrediente() != null) {
                    necessidade.merge(opcao.getIngredienteId(),
                            opcao.getQuantidadeIngrediente().multiply(porcoes), BigDecimal::add);
                }
            }
        }
        return necessidade;
    }

    // RN03: se faltar estoque de algum ingrediente, lanca 422 dizendo o que falta.
    public void conferir(Map<Long, BigDecimal> necessidade) {
        if (necessidade.isEmpty()) {
            return;
        }
        Map<Long, BigDecimal> saldos = buscarSaldos(necessidade.keySet());
        Map<Long, Ingrediente> ingredientes = buscarIngredientes(necessidade.keySet());

        List<String> faltas = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entrada : necessidade.entrySet()) {
            BigDecimal saldo = saldos.getOrDefault(entrada.getKey(), BigDecimal.ZERO);
            if (saldo.compareTo(entrada.getValue()) < 0) {
                Ingrediente ingrediente = ingredientes.get(entrada.getKey());
                String nome = (ingrediente == null) ? "Ingrediente " + entrada.getKey() : ingrediente.getNome();
                String unidade = (ingrediente == null) ? "" : " " + ingrediente.getUnidadePadrao().toLowerCase();
                faltas.add(nome + ": o pedido precisa de " + numero(entrada.getValue()) + unidade
                        + " e há " + numero(saldo.max(BigDecimal.ZERO)) + unidade);
            }
        }
        if (!faltas.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Não temos estoque para este pedido agora.", faltas);
        }
    }

    // RF-017 e RF-029: baixa automatica. Confere o saldo de novo (ele pode ter
    // mudado desde o checkout) e grava uma SAIDA/VENDA por ingrediente, ligada ao pedido.
    // Sem @Transactional proprio de proposito: roda dentro da transacao de quem
    // chama, entao se a troca de status falhar a baixa e desfeita junto.
    public void baixarPorPedido(Pedido pedido, Long usuarioId) {
        Map<Long, BigDecimal> necessidade = calcularNecessidade(pedido);
        conferir(necessidade);
        Map<Long, Ingrediente> ingredientes = buscarIngredientes(necessidade.keySet());

        for (Map.Entry<Long, BigDecimal> entrada : necessidade.entrySet()) {
            if (entrada.getValue().signum() <= 0) {
                continue;
            }
            Ingrediente ingrediente = ingredientes.get(entrada.getKey());
            EstoqueMovimentacao saida = new EstoqueMovimentacao();
            saida.setIngredienteId(entrada.getKey());
            saida.setTipo("SAIDA");
            saida.setMotivo("VENDA");
            saida.setQuantidade(entrada.getValue());
            saida.setCustoUnitario(ingrediente == null ? null : ingrediente.getCustoUnitario());
            saida.setPedidoId(pedido.getId());
            saida.setUsuarioId(usuarioId);
            estoqueRepository.save(saida);
        }
    }

    // RF-018: cancelamento devolve ao estoque. Para cada SAIDA gerada pelo
    // pedido, grava um ESTORNO do mesmo tamanho. Pedido que ainda nao tinha
    // sido confirmado nao tem SAIDA, entao nada e gravado.
    public void estornarPedido(Pedido pedido, Long usuarioId) {
        for (EstoqueMovimentacao saida : estoqueRepository.findByPedidoIdAndTipo(pedido.getId(), "SAIDA")) {
            EstoqueMovimentacao estorno = new EstoqueMovimentacao();
            estorno.setIngredienteId(saida.getIngredienteId());
            estorno.setTipo("ESTORNO");
            estorno.setMotivo("ESTORNO");
            estorno.setQuantidade(saida.getQuantidade());
            estorno.setCustoUnitario(saida.getCustoUnitario());
            estorno.setPedidoId(pedido.getId());
            estorno.setUsuarioId(usuarioId);
            estoqueRepository.save(estorno);
        }
    }

    // Saldo atual de cada ingrediente (soma das entradas e estornos menos as saidas).
    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> buscarSaldos(Set<Long> ingredienteIds) {
        Map<Long, BigDecimal> saldos = new HashMap<>();
        if (ingredienteIds.isEmpty()) {
            return saldos;
        }
        for (Object[] linha : estoqueRepository.somarSaldos(ingredienteIds)) {
            saldos.put(((Number) linha[0]).longValue(), new BigDecimal(linha[1].toString()));
        }
        return saldos;
    }

    private Map<Long, Ingrediente> buscarIngredientes(Set<Long> ids) {
        Map<Long, Ingrediente> ingredientes = new HashMap<>();
        for (Ingrediente ingrediente : ingredienteRepository.findAllById(ids)) {
            ingredientes.put(ingrediente.getId(), ingrediente);
        }
        return ingredientes;
    }

    // 80.000 vira "80"; 12.500 vira "12.5".
    private static String numero(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }
}
