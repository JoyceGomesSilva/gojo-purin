package br.com.gojopurin.backend.service;

import br.com.gojopurin.backend.dto.DashboardResumoResponse;
import br.com.gojopurin.backend.dto.TopPratoResponse;
import br.com.gojopurin.backend.dto.VendaDiaResponse;
import br.com.gojopurin.backend.exception.ApiException;
import br.com.gojopurin.backend.repository.PedidoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Numeros do dashboard (RF-034 a RF-037). As contas pesadas sao queries no
// PedidoRepository; aqui ficam as regras (o que e "hoje", ticket medio,
// food cost) e a montagem das respostas.
@Service
public class DashboardService {

    // "Hoje" e o dia de Sao Paulo, mesmo que o servidor (Render) esteja em
    // outro fuso. Sem isso, as vendas da noite cairiam no dia seguinte.
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final int MAXIMO_DE_DIAS = 366;

    private final PedidoRepository pedidoRepository;
    private final FichaTecnicaService fichaTecnicaService;
    private final AdminEstoqueService adminEstoqueService;

    public DashboardService(PedidoRepository pedidoRepository,
                            FichaTecnicaService fichaTecnicaService,
                            AdminEstoqueService adminEstoqueService) {
        this.pedidoRepository = pedidoRepository;
        this.fichaTecnicaService = fichaTecnicaService;
        this.adminEstoqueService = adminEstoqueService;
    }

    // RF-034: faturamento, pedidos, ticket medio e food cost de hoje.
    @Transactional(readOnly = true)
    public DashboardResumoResponse resumo() {
        LocalDate hoje = LocalDate.now(FUSO);
        LocalDateTime inicio = inicioDoDia(hoje);
        LocalDateTime fim = inicioDoDia(hoje.plusDays(1));

        Object[] linha = pedidoRepository.resumir(inicio, fim).get(0);
        long pedidos = ((Number) linha[0]).longValue();
        BigDecimal faturamento = decimal(linha[1]);

        // Food cost do dia = quanto custaram os pratos vendidos / quanto entrou.
        // O custo de cada prato vem da ficha tecnica, pela mesma conta da tela
        // da ficha (FichaTecnicaService), com o custo atual dos ingredientes.
        BigDecimal custoDosPratos = BigDecimal.ZERO;
        for (Object[] vendido : pedidoRepository.maisVendidos(inicio, fim, Pageable.unpaged())) {
            Long pratoId = (Long) vendido[0];
            long unidades = ((Number) vendido[2]).longValue();
            BigDecimal custoPorcao = fichaTecnicaService.custo(pratoId).custoPorcao();
            custoDosPratos = custoDosPratos.add(custoPorcao.multiply(BigDecimal.valueOf(unidades)));
        }

        BigDecimal foodCost = null;
        String faixa = null;
        if (faturamento.signum() > 0) {
            foodCost = custoDosPratos.multiply(CEM).divide(faturamento, 1, RoundingMode.HALF_UP);
            faixa = FichaTecnicaService.faixaDe(foodCost);
        }

        return new DashboardResumoResponse(
                hoje,
                faturamento.setScale(2, RoundingMode.HALF_UP),
                pedidos,
                ticketMedio(faturamento, pedidos),
                foodCost,
                faixa,
                adminEstoqueService.listarAlertas().size());
    }

    // RF-035: os 5 pratos mais vendidos no periodo (em unidades).
    @Transactional(readOnly = true)
    public List<TopPratoResponse> topPratos(LocalDate de, LocalDate ate) {
        conferirPeriodo(de, ate);
        List<TopPratoResponse> ranking = new ArrayList<>();
        for (Object[] linha : pedidoRepository.maisVendidos(inicioDoDia(de), inicioDoDia(ate.plusDays(1)),
                PageRequest.of(0, 5))) {
            ranking.add(new TopPratoResponse(
                    (Long) linha[0],
                    (String) linha[1],
                    ((Number) linha[2]).longValue(),
                    decimal(linha[3]).setScale(2, RoundingMode.HALF_UP)));
        }
        return ranking;
    }

    // RF-037: faturamento de cada dia do periodo, incluindo os dias sem venda
    // (com zero), para o grafico nao "pular" dias.
    @Transactional(readOnly = true)
    public List<VendaDiaResponse> vendasPorDia(LocalDate de, LocalDate ate) {
        conferirPeriodo(de, ate);

        Map<LocalDate, BigDecimal> faturamentoPorDia = new HashMap<>();
        Map<LocalDate, Long> pedidosPorDia = new HashMap<>();
        for (Object[] linha : pedidoRepository.vendasNoPeriodo(inicioDoDia(de), inicioDoDia(ate.plusDays(1)))) {
            LocalDate dia = diaDe((LocalDateTime) linha[0]);
            faturamentoPorDia.merge(dia, decimal(linha[1]), BigDecimal::add);
            pedidosPorDia.merge(dia, 1L, Long::sum);
        }

        List<VendaDiaResponse> dias = new ArrayList<>();
        for (LocalDate dia = de; !dia.isAfter(ate); dia = dia.plusDays(1)) {
            BigDecimal faturamento = faturamentoPorDia.getOrDefault(dia, BigDecimal.ZERO);
            long pedidos = pedidosPorDia.getOrDefault(dia, 0L);
            dias.add(new VendaDiaResponse(dia, pedidos, faturamento.setScale(2, RoundingMode.HALF_UP),
                    ticketMedio(faturamento, pedidos)));
        }
        return dias;
    }

    // ---------- Internos ----------

    // Meia-noite do dia em Sao Paulo, convertida para o horario do servidor
    // (que e o horario em que os pedidos sao gravados).
    private LocalDateTime inicioDoDia(LocalDate dia) {
        return dia.atStartOfDay(FUSO).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    // O caminho inverso: em que dia de Sao Paulo um pedido foi feito.
    private LocalDate diaDe(LocalDateTime criadoEm) {
        return criadoEm.atZone(ZoneId.systemDefault()).withZoneSameInstant(FUSO).toLocalDate();
    }

    private BigDecimal ticketMedio(BigDecimal faturamento, long pedidos) {
        if (pedidos == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return faturamento.divide(BigDecimal.valueOf(pedidos), 2, RoundingMode.HALF_UP);
    }

    private void conferirPeriodo(LocalDate de, LocalDate ate) {
        if (de.isAfter(ate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data inicial deve ser antes da final");
        }
        if (ChronoUnit.DAYS.between(de, ate) >= MAXIMO_DE_DIAS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escolha um período de no máximo 1 ano");
        }
    }

    // A soma vem do banco como BigDecimal, Long ou Integer, dependendo do caso.
    private static BigDecimal decimal(Object valor) {
        return valor == null ? BigDecimal.ZERO : new BigDecimal(valor.toString());
    }
}
