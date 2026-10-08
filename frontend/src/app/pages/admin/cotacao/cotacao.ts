import { Component, DestroyRef, ElementRef, afterRenderEffect, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { Chart, type ChartConfiguration, registerables } from 'chart.js';
import { AdminFichaService } from '../../../core/admin-ficha.service';
import { AdminFornecedorService } from '../../../core/admin-fornecedor.service';
import { Cotacao, IngredienteOpcao } from '../../../core/models';
import { formatarCustoUnitario } from '../../../core/quantidade';

// Chart.js precisa saber quais tipos de grafico, eixos e legendas vai usar.
// "registerables" registra todos de uma vez.
Chart.register(...registerables);

// Uma cor por fornecedor no grafico, sempre nesta ordem. Paleta conferida
// para daltonismo (cores distinguiveis entre si); a legenda embaixo do
// grafico e a lista de ofertas garantem que nada dependa so da cor.
const CORES = ['#2a78d6', '#eb6834', '#1baf7a', '#eda100', '#e87ba4'];

const formatadorDeData = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
const formatadorDePreco = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4
});

// Cotacao comparativa (RF-023): escolhe um ingrediente e ve todos os
// fornecedores que vendem, do mais barato para o mais caro. Embaixo, o
// grafico de evolucao dos precos (RF-026), feito com Chart.js.
@Component({
  selector: 'app-admin-cotacao',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './cotacao.html',
  styleUrls: ['../admin-crud.scss', './cotacao.scss']
})
export class AdminCotacao {
  private servico = inject(AdminFornecedorService);
  private fichaServico = inject(AdminFichaService);
  private rota = inject(ActivatedRoute);
  private router = inject(Router);

  protected formatarCustoUnitario = formatarCustoUnitario;

  protected ingredientes = signal<IngredienteOpcao[]>([]);
  protected ingredienteId = signal<number | null>(null);
  protected cotacao = signal<Cotacao | null>(null);
  protected carregando = signal(false);
  protected falhou = signal(false);

  // O <canvas #grafico> do HTML. viewChild e um signal: fica undefined
  // enquanto o canvas nao esta na tela.
  private canvas = viewChild<ElementRef<HTMLCanvasElement>>('grafico');
  private grafico: Chart | null = null;

  constructor() {
    this.fichaServico.ingredientes().subscribe({
      next: (ingredientes) => this.ingredientes.set(ingredientes.filter((item) => item.status === 'ATIVO'))
    });

    // Veio de um link "ver cotacao" (?ingrediente=12)? Ja abre nele.
    const doEndereco = Number(this.rota.snapshot.queryParamMap.get('ingrediente'));
    if (doEndereco) {
      this.buscar(doEndereco);
    }

    // Redesenha o grafico depois que a tela atualiza, sempre que a cotacao
    // ou o canvas mudarem. O grafico antigo e destruido antes, senao o
    // Chart.js reclama que o canvas ja esta em uso.
    afterRenderEffect(() => {
      const canvas = this.canvas();
      const cotacao = this.cotacao();
      this.grafico?.destroy();
      this.grafico = null;
      if (canvas && cotacao && cotacao.historico.length > 0) {
        this.grafico = new Chart(canvas.nativeElement, this.configuracao(cotacao));
      }
    });
    inject(DestroyRef).onDestroy(() => this.grafico?.destroy());
  }

  escolher(evento: Event): void {
    const id = Number((evento.target as HTMLSelectElement).value);
    if (id) {
      // Guarda o ingrediente no endereco, para o link poder ser compartilhado.
      this.router.navigate([], { queryParams: { ingrediente: id } });
      this.buscar(id);
    }
  }

  // "+12,5%" ou "mais barato".
  diferenca(percentual: number): string {
    return `+${percentual.toLocaleString('pt-BR', { maximumFractionDigits: 1 })}%`;
  }

  private buscar(id: number): void {
    this.ingredienteId.set(id);
    this.carregando.set(true);
    this.falhou.set(false);
    this.servico.cotacao(id).subscribe({
      next: (cotacao) => {
        this.cotacao.set(cotacao);
        this.carregando.set(false);
      },
      error: () => {
        this.cotacao.set(null);
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  // Monta os dados do grafico de linhas: uma linha por fornecedor.
  // Eixo X: os momentos em que algum preco mudou. Entre uma mudanca e outra
  // o preco continua o mesmo, por isso o grafico e "em degraus" (stepped).
  private configuracao(cotacao: Cotacao): ChartConfiguration<'line', (number | null)[], string> {
    const momentos = [...new Set(cotacao.historico.map((ponto) => ponto.dataHora))];
    const rotulos = momentos.map((momento) => formatadorDeData.format(new Date(momento)));

    // Lista sem repeticao de [id, nome] dos fornecedores que aparecem no historico.
    const fornecedores = [
      ...new Map(cotacao.historico.map((ponto): [number, string] => [ponto.fornecedorId, ponto.razaoSocial]))
    ];

    const linhas = fornecedores.map(([fornecedorId, razaoSocial], indice) => {
      let ultimoPreco: number | null = null;
      const valores = momentos.map((momento) => {
        const ponto = cotacao.historico.find(
          (item) => item.fornecedorId === fornecedorId && item.dataHora === momento
        );
        if (ponto) {
          ultimoPreco = ponto.preco;
        }
        return ultimoPreco; // repete o ultimo preco conhecido ate o proximo
      });
      const cor = CORES[indice % CORES.length];
      return {
        label: razaoSocial,
        data: valores,
        borderColor: cor,
        backgroundColor: cor,
        stepped: true,
        pointRadius: 4
      };
    });

    const unidade = cotacao.unidade.toLowerCase();
    return {
      type: 'line',
      data: { labels: rotulos, datasets: linhas },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: (contexto) => `${contexto.dataset.label}: ${formatadorDePreco.format(Number(contexto.parsed.y))}/${unidade}`
            }
          }
        },
        scales: {
          y: {
            ticks: { callback: (valor) => formatadorDePreco.format(Number(valor)) }
          }
        }
      }
    };
  }
}
