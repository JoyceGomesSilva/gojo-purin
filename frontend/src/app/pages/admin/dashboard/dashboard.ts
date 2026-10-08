import { Component, DestroyRef, ElementRef, afterRenderEffect, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { Chart, type ChartConfiguration, registerables } from 'chart.js';
import { forkJoin } from 'rxjs';
import { AdminDashboardService } from '../../../core/admin-dashboard.service';
import { AdminEstoqueService } from '../../../core/admin-estoque.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DashboardResumo, FaixaFoodCost, SaldoEstoque, TopPrato, VendaDia } from '../../../core/models';
import { formatarQuantidade } from '../../../core/quantidade';

Chart.register(...registerables);

// Cores dos graficos: uma serie so em cada um, entao uma cor so (o azul da
// paleta conferida para daltonismo). Grade e eixos em tons apagados, para o
// dado aparecer mais que a moldura.
const COR_SERIE = '#2a78d6';
const COR_GRADE = '#ece6f5';
const COR_TEXTO = '#6d6890';

const formatadorDeDinheiro = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

const NOMES_DAS_FAIXAS: Record<FaixaFoodCost, string> = {
  VERDE: 'Saudável (até 30%)',
  AMARELO: 'Atenção (de 30% a 35%)',
  VERMELHO: 'Alto (acima de 35%)'
};

// "2026-10-08" de hoje, no horario do computador de quem esta usando.
function dataISO(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}

function diasAtras(quantos: number): string {
  const data = new Date();
  data.setDate(data.getDate() - quantos);
  return dataISO(data);
}

// "2026-10-08" vira "08/10". Feito com texto, sem new Date(), para o fuso
// horario nao trocar o dia.
export function diaCurto(iso: string): string {
  const [, mes, dia] = iso.split('-');
  return `${dia}/${mes}`;
}

// Dashboard (RF-034 a RF-037): cards de hoje, alertas de estoque, top 5
// pratos e vendas por dia no periodo escolhido.
@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink, MatButtonModule, BrlPipe],
  templateUrl: './dashboard.html',
  styleUrls: ['../admin-crud.scss', './dashboard.scss']
})
export class AdminDashboard {
  private servico = inject(AdminDashboardService);
  private estoque = inject(AdminEstoqueService);

  protected formatarQuantidade = formatarQuantidade;
  protected diaCurto = diaCurto;
  protected nomeDaFaixa = (faixa: FaixaFoodCost) => NOMES_DAS_FAIXAS[faixa];
  protected porcento = (valor: number) =>
    valor.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '%';

  // ----- Hoje -----
  protected resumo = signal<DashboardResumo | null>(null);
  protected alertas = signal<SaldoEstoque[]>([]);
  protected carregandoHoje = signal(true);
  protected falhouHoje = signal(false);

  // ----- Periodo (top 5 e vendas) -----
  protected de = signal(diasAtras(6));
  protected ate = signal(diasAtras(0));
  protected top = signal<TopPrato[]>([]);
  protected vendas = signal<VendaDia[]>([]);
  protected carregandoPeriodo = signal(true);
  protected falhouPeriodo = signal(false);

  // Totais do periodo, para a ultima linha da tabela.
  protected totalPedidos = computed(() => this.vendas().reduce((total, dia) => total + dia.pedidos, 0));
  protected totalFaturamento = computed(() => this.vendas().reduce((total, dia) => total + dia.faturamento, 0));
  protected ticketDoPeriodo = computed(() =>
    this.totalPedidos() > 0 ? this.totalFaturamento() / this.totalPedidos() : 0
  );

  // ----- Graficos (Chart.js) -----
  private canvasTop = viewChild<ElementRef<HTMLCanvasElement>>('graficoTop');
  private canvasVendas = viewChild<ElementRef<HTMLCanvasElement>>('graficoVendas');
  private graficoTop: Chart | null = null;
  private graficoVendas: Chart | null = null;

  constructor() {
    this.carregarHoje();
    this.carregarPeriodo();

    // Depois que a tela atualiza, (re)desenha cada grafico com os dados novos.
    afterRenderEffect(() => {
      const canvas = this.canvasTop();
      const top = this.top();
      this.graficoTop?.destroy();
      this.graficoTop = canvas && top.length > 0 ? new Chart(canvas.nativeElement, this.configuracaoTop(top)) : null;
    });
    afterRenderEffect(() => {
      const canvas = this.canvasVendas();
      const vendas = this.vendas();
      this.graficoVendas?.destroy();
      this.graficoVendas =
        canvas && vendas.length > 0 ? new Chart(canvas.nativeElement, this.configuracaoVendas(vendas)) : null;
    });
    inject(DestroyRef).onDestroy(() => {
      this.graficoTop?.destroy();
      this.graficoVendas?.destroy();
    });
  }

  // ---------- Filtro de periodo ----------

  mudarData(campo: 'de' | 'ate', evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value;
    if (!valor) {
      return;
    }
    (campo === 'de' ? this.de : this.ate).set(valor);
    this.carregarPeriodo();
  }

  // Atalhos: ultimos 7 ou 30 dias, contando hoje.
  ultimosDias(quantos: number): void {
    this.de.set(diasAtras(quantos - 1));
    this.ate.set(diasAtras(0));
    this.carregarPeriodo();
  }

  tentarDeNovo(): void {
    this.carregarHoje();
    this.carregarPeriodo();
  }

  // ---------- Carregamento ----------

  private carregarHoje(): void {
    this.carregandoHoje.set(true);
    this.falhouHoje.set(false);
    // alertas() tambem atualiza o selo vermelho do menu.
    forkJoin({ resumo: this.servico.resumo(), alertas: this.estoque.alertas() }).subscribe({
      next: ({ resumo, alertas }) => {
        this.resumo.set(resumo);
        this.alertas.set(alertas);
        this.carregandoHoje.set(false);
      },
      error: () => {
        this.falhouHoje.set(true);
        this.carregandoHoje.set(false);
      }
    });
  }

  private carregarPeriodo(): void {
    this.carregandoPeriodo.set(true);
    this.falhouPeriodo.set(false);
    forkJoin({
      top: this.servico.topPratos(this.de(), this.ate()),
      vendas: this.servico.vendas(this.de(), this.ate())
    }).subscribe({
      next: ({ top, vendas }) => {
        this.top.set(top);
        this.vendas.set(vendas);
        this.carregandoPeriodo.set(false);
      },
      error: () => {
        this.falhouPeriodo.set(true);
        this.carregandoPeriodo.set(false);
      }
    });
  }

  // ---------- Configuracao dos graficos ----------

  // RF-035: barras deitadas, o mais vendido em cima. Uma serie so, sem legenda:
  // o titulo do cartao ja diz o que e.
  private configuracaoTop(top: TopPrato[]): ChartConfiguration<'bar', number[], string> {
    return {
      type: 'bar',
      data: {
        labels: top.map((prato) => prato.nome),
        datasets: [
          {
            label: 'Unidades vendidas',
            data: top.map((prato) => prato.quantidade),
            backgroundColor: COR_SERIE,
            borderRadius: 4,
            maxBarThickness: 28
          }
        ]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (contexto) => {
                const prato = top[contexto.dataIndex];
                return `${prato.quantidade} un. · ${formatadorDeDinheiro.format(prato.faturamento)}`;
              }
            }
          }
        },
        scales: {
          x: {
            beginAtZero: true,
            ticks: { precision: 0, color: COR_TEXTO },
            grid: { color: COR_GRADE }
          },
          y: { ticks: { color: COR_TEXTO }, grid: { display: false } }
        }
      }
    };
  }

  // RF-037: faturamento por dia, em linha.
  private configuracaoVendas(vendas: VendaDia[]): ChartConfiguration<'line', number[], string> {
    return {
      type: 'line',
      data: {
        labels: vendas.map((dia) => diaCurto(dia.dia)),
        datasets: [
          {
            label: 'Faturamento',
            data: vendas.map((dia) => dia.faturamento),
            borderColor: COR_SERIE,
            backgroundColor: COR_SERIE,
            borderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6,
            tension: 0.2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        // A dica aparece ao passar o mouse em qualquer ponto da coluna do dia.
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (contexto) => {
                const dia = vendas[contexto.dataIndex];
                return `${formatadorDeDinheiro.format(dia.faturamento)} · ${dia.pedidos} ${dia.pedidos === 1 ? 'pedido' : 'pedidos'}`;
              }
            }
          }
        },
        scales: {
          x: { ticks: { color: COR_TEXTO }, grid: { display: false } },
          y: {
            beginAtZero: true,
            ticks: { color: COR_TEXTO, callback: (valor) => formatadorDeDinheiro.format(Number(valor)) },
            grid: { color: COR_GRADE }
          }
        }
      }
    };
  }
}
