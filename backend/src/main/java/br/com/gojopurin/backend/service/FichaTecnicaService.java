package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.CustoResponse;
import br.com.gojopurin.backend.dto.FichaTecnicaItemRequest;
import br.com.gojopurin.backend.dto.FichaTecnicaItemResponse;
import br.com.gojopurin.backend.dto.FichaTecnicaRequest;
import br.com.gojopurin.backend.dto.FichaTecnicaResponse;
import br.com.gojopurin.backend.dto.IngredienteOpcaoResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.FichaTecnica;
import br.com.gojopurin.backend.model.FichaTecnicaItem;
import br.com.gojopurin.backend.model.Ingrediente;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.repository.FichaTecnicaRepository;
import br.com.gojopurin.backend.repository.IngredienteRepository;
import br.com.gojopurin.backend.repository.PratoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Ficha tecnica, custo do prato e food cost (RF-011 a RF-013, RN02).
// Todas as contas ficam aqui, em um lugar so: o metodo montar().
@Service
public class FichaTecnicaService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    // Limites do food cost (RF-013): ate 30% verde, ate 35% amarelo, acima vermelho.
    private static final BigDecimal LIMITE_VERDE = BigDecimal.valueOf(30);
    private static final BigDecimal LIMITE_AMARELO = BigDecimal.valueOf(35);

    private final PratoRepository pratoRepository;
    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final IngredienteRepository ingredienteRepository;

    public FichaTecnicaService(PratoRepository pratoRepository,
                               FichaTecnicaRepository fichaTecnicaRepository,
                               IngredienteRepository ingredienteRepository) {
        this.pratoRepository = pratoRepository;
        this.fichaTecnicaRepository = fichaTecnicaRepository;
        this.ingredienteRepository = ingredienteRepository;
    }

    // Uma linha da receita, pronta para a conta. Tanto a ficha salva no banco
    // quanto a que o painel envia sao convertidas para este formato, e assim
    // a conta e escrita uma vez so.
    private record Linha(Ingrediente ingrediente, BigDecimal quantidade, BigDecimal fatorCorrecao) {
    }

    // Os ingredientes para o campo de escolha da tela.
    @Transactional(readOnly = true)
    public List<IngredienteOpcaoResponse> listarIngredientes() {
        return ingredienteRepository.findAllByOrderByNomeAsc().stream()
                .map(IngredienteOpcaoResponse::de)
                .toList();
    }

    // A ficha salva do prato. Se ele ainda nao tem ficha, devolve uma vazia
    // (rendimento 1, sem ingredientes) para a tela ja abrir pronta para preencher.
    @Transactional(readOnly = true)
    public FichaTecnicaResponse buscar(Long pratoId) {
        Prato prato = buscarPrato(pratoId);
        FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(pratoId).orElse(null);
        if (ficha == null) {
            return montar(prato, 1, null, List.of());
        }
        return montar(prato, ficha.getRendimento(), ficha.getModoPreparo(), linhasDaFicha(ficha));
    }

    // So os numeros do custo (GET /api/admin/pratos/{id}/custo).
    @Transactional(readOnly = true)
    public CustoResponse custo(Long pratoId) {
        return CustoResponse.de(buscar(pratoId));
    }

    // Faz as contas com os dados que estao na tela, SEM salvar. E o que deixa
    // o custo aparecer "em tempo real" enquanto a pessoa edita (RF-012).
    @Transactional(readOnly = true)
    public FichaTecnicaResponse simular(Long pratoId, FichaTecnicaRequest request) {
        Prato prato = buscarPrato(pratoId);
        return montar(prato, request.rendimento(), request.modoPreparo(), linhasDoRequest(request));
    }

    // Salva a ficha: cria se o prato ainda nao tem, atualiza se ja tem.
    @Transactional
    public FichaTecnicaResponse salvar(Long pratoId, FichaTecnicaRequest request) {
        Prato prato = buscarPrato(pratoId);
        List<Linha> linhas = linhasDoRequest(request);

        // RF-014: toda ficha salva precisa do passo a passo para a cozinha.
        if (request.modoPreparo() == null || request.modoPreparo().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Informe o modo de preparo");
        }
        String modoPreparo = request.modoPreparo().trim();

        // RN01 pelo outro lado: prato ATIVO nao pode ficar sem ingredientes.
        if (linhas.isEmpty() && "ATIVO".equals(prato.getStatus())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Este prato está ativo e precisa de pelo menos 1 ingrediente. "
                            + "Pause ou desative o prato antes de esvaziar a ficha.");
        }

        FichaTecnica ficha = fichaTecnicaRepository.findByPratoId(pratoId).orElse(null);
        if (ficha == null) {
            ficha = new FichaTecnica();
            ficha.setPrato(prato);
        }
        ficha.setRendimento(request.rendimento());
        ficha.setModoPreparo(modoPreparo);

        // Quais ingredientes continuam na receita.
        Set<Long> idsQueFicam = new HashSet<>();
        for (Linha linha : linhas) {
            idsQueFicam.add(linha.ingrediente().getId());
        }

        // 1. Tira da ficha os ingredientes que sairam da receita.
        ficha.getItens().removeIf(item -> !idsQueFicam.contains(item.getIngrediente().getId()));

        // 2. Atualiza os que ja existiam e acrescenta os novos.
        Map<Long, FichaTecnicaItem> itensAtuais = new HashMap<>();
        for (FichaTecnicaItem item : ficha.getItens()) {
            itensAtuais.put(item.getIngrediente().getId(), item);
        }
        for (Linha linha : linhas) {
            FichaTecnicaItem item = itensAtuais.get(linha.ingrediente().getId());
            if (item == null) {
                item = new FichaTecnicaItem();
                item.setFichaTecnica(ficha);
                item.setIngrediente(linha.ingrediente());
                ficha.getItens().add(item);
            }
            item.setQuantidade(linha.quantidade());
            item.setFatorCorrecao(linha.fatorCorrecao());
            // A unidade e sempre a do ingrediente: e nela que o estoque da baixa.
            item.setUnidade(linha.ingrediente().getUnidadePadrao());
        }

        fichaTecnicaRepository.save(ficha);
        return montar(prato, request.rendimento(), modoPreparo, linhas);
    }

    // ---------- A conta (RF-012, RF-013, RN02) ----------

    private FichaTecnicaResponse montar(Prato prato, int rendimento, String modoPreparo, List<Linha> linhas) {
        List<FichaTecnicaItemResponse> itens = new ArrayList<>();
        BigDecimal custoTotal = BigDecimal.ZERO;

        for (Linha linha : linhas) {
            Ingrediente ingrediente = linha.ingrediente();
            // custo da linha = quantidade x fator de correcao x custo unitario,
            // arredondado para centavos. O total soma os valores ja arredondados,
            // para a tabela da tela "fechar" com o total.
            BigDecimal custo = linha.quantidade()
                    .multiply(linha.fatorCorrecao())
                    .multiply(ingrediente.getCustoUnitario())
                    .setScale(2, RoundingMode.HALF_UP);
            custoTotal = custoTotal.add(custo);

            itens.add(new FichaTecnicaItemResponse(
                    ingrediente.getId(),
                    ingrediente.getNome(),
                    ingrediente.getUnidadePadrao(),
                    linha.quantidade(),
                    linha.fatorCorrecao(),
                    ingrediente.getCustoUnitario(),
                    custo));
        }

        // custo de uma porcao = custo total / rendimento
        BigDecimal custoPorcao = custoTotal.divide(BigDecimal.valueOf(rendimento), 2, RoundingMode.HALF_UP);

        BigDecimal foodCost = null;
        String faixa = null;
        String aviso = null;
        // Sem ingredientes ou sem preco nao ha food cost para mostrar.
        if (!linhas.isEmpty() && prato.getPrecoVenda().signum() > 0) {
            // food cost = (custo da porcao / preco de venda) x 100, com 1 casa decimal
            foodCost = custoPorcao.multiply(CEM).divide(prato.getPrecoVenda(), 1, RoundingMode.HALF_UP);
            faixa = faixaDe(foodCost);
            if ("VERMELHO".equals(faixa)) {
                // RN02: so avisa, nao bloqueia.
                aviso = "Food cost de " + foodCost.toPlainString().replace('.', ',')
                        + "% está acima de 35%. Reveja o preço de venda ou a receita.";
            }
        }

        return new FichaTecnicaResponse(
                prato.getId(),
                prato.getNome(),
                prato.getStatus(),
                prato.getPrecoVenda(),
                rendimento,
                modoPreparo,
                itens,
                custoTotal,
                custoPorcao,
                foodCost,
                faixa,
                aviso);
    }

    // RF-013: verde ate 30%, amarelo ate 35%, vermelho acima disso.
    // "static" e sem "private": o DashboardService usa a mesma regra.
    static String faixaDe(BigDecimal foodCost) {
        if (foodCost.compareTo(LIMITE_VERDE) <= 0) {
            return "VERDE";
        }
        if (foodCost.compareTo(LIMITE_AMARELO) <= 0) {
            return "AMARELO";
        }
        return "VERMELHO";
    }

    // ---------- Conversoes para Linha ----------

    // Da ficha salva no banco, em ordem alfabetica de ingrediente.
    private List<Linha> linhasDaFicha(FichaTecnica ficha) {
        return ficha.getItens().stream()
                .map(item -> new Linha(item.getIngrediente(), item.getQuantidade(), item.getFatorCorrecao()))
                .sorted(Comparator.comparing(linha -> linha.ingrediente().getNome()))
                .toList();
    }

    // Do que o painel enviou. Confere se cada ingrediente existe e se nenhum
    // aparece duas vezes (o banco tambem proibe: UNIQUE ficha + ingrediente).
    private List<Linha> linhasDoRequest(FichaTecnicaRequest request) {
        List<Long> ids = request.itens().stream().map(FichaTecnicaItemRequest::ingredienteId).toList();

        Map<Long, Ingrediente> ingredientes = new HashMap<>();
        for (Ingrediente ingrediente : ingredienteRepository.findAllById(ids)) {
            ingredientes.put(ingrediente.getId(), ingrediente);
        }

        List<Linha> linhas = new ArrayList<>();
        Set<Long> jaVistos = new HashSet<>();
        for (FichaTecnicaItemRequest item : request.itens()) {
            Ingrediente ingrediente = ingredientes.get(item.ingredienteId());
            if (ingrediente == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Ingrediente não encontrado");
            }
            // add devolve false quando o id ja estava no conjunto.
            if (!jaVistos.add(ingrediente.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "O ingrediente " + ingrediente.getNome() + " aparece mais de uma vez na ficha");
            }
            linhas.add(new Linha(ingrediente, item.quantidade(), item.fatorCorrecao()));
        }
        return linhas;
    }

    private Prato buscarPrato(Long pratoId) {
        return pratoRepository.findById(pratoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Prato não encontrado"));
    }
}
