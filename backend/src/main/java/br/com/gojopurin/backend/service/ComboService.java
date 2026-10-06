package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.ComboEtapaResponse;
import br.com.gojopurin.backend.dto.ComboResponse;
import br.com.gojopurin.backend.dto.PratoCardapioResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.model.Combo;
import br.com.gojopurin.backend.model.ComboEtapa;
import br.com.gojopurin.backend.model.Prato;
import br.com.gojopurin.backend.repository.ComboRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Regras dos combos no cardapio publico.
@Service
@Transactional(readOnly = true)
public class ComboService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final ComboRepository comboRepository;

    public ComboService(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    public List<ComboResponse> listar() {
        return comboRepository.findByStatusOrderByOrdemAscNomeAsc("ATIVO").stream()
                .map(this::montar)
                .flatMap(Optional::stream)
                .toList();
    }

    public ComboResponse detalhar(Long id) {
        return comboRepository.findByIdAndStatus(id, "ATIVO")
                .flatMap(this::montar)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Combo não encontrado"));
    }

    // Preco de um prato dentro do combo: preco x (100 - desconto) / 100,
    // arredondado para centavos. O desconto vale por prato, e o total do combo
    // e a soma dos pratos ja com desconto. O front usa a mesma regra.
    public static BigDecimal aplicarDesconto(BigDecimal preco, BigDecimal descontoPercentual) {
        return preco.multiply(CEM.subtract(descontoPercentual)).divide(CEM, 2, RoundingMode.HALF_UP);
    }

    // Monta a resposta com apenas os pratos ATIVOS de cada etapa.
    // Se uma etapa obrigatoria ficar sem nenhum prato (ex.: o prato foi pausado),
    // o combo nao pode ser vendido e some do cardapio (Optional vazio).
    private Optional<ComboResponse> montar(Combo combo) {
        List<ComboEtapaResponse> etapas = combo.getEtapas().stream()
                .map(this::montarEtapa)
                .filter(etapa -> etapa.obrigatoria() || !etapa.pratos().isEmpty())
                .toList();

        boolean faltaPrato = etapas.stream().anyMatch(etapa -> etapa.pratos().isEmpty());
        if (etapas.isEmpty() || faltaPrato) {
            return Optional.empty();
        }

        BigDecimal precoCheio = null;
        BigDecimal precoComDesconto = null;
        if ("PRONTO".equals(combo.getTipo())) {
            // No combo pronto cada etapa tem um prato so: da para somar.
            List<BigDecimal> precos = etapas.stream().map(etapa -> etapa.pratos().get(0).preco()).toList();
            precoCheio = precos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            precoComDesconto = precos.stream()
                    .map(preco -> aplicarDesconto(preco, combo.getDescontoPercentual()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return Optional.of(new ComboResponse(
                combo.getId(),
                combo.getNome(),
                combo.getDescricao(),
                combo.getTipo(),
                combo.getDescontoPercentual(),
                combo.getFotoUrl(),
                precoCheio,
                precoComDesconto,
                etapas));
    }

    private ComboEtapaResponse montarEtapa(ComboEtapa etapa) {
        List<PratoCardapioResponse> pratos = etapa.getPratos().stream()
                .filter(prato -> "ATIVO".equals(prato.getStatus()))
                .filter(prato -> "ATIVO".equals(prato.getCategoria().getStatus()))
                .sorted(Comparator
                        .comparing((Prato prato) -> prato.getCategoria().getOrdem())
                        .thenComparing(Prato::getNome))
                .map(PratoCardapioResponse::de)
                .toList();
        return new ComboEtapaResponse(etapa.getId(), etapa.getNome(), etapa.isObrigatoria(), pratos);
    }
}
