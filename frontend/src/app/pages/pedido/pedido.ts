import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { BrlPipe } from '../../core/brl.pipe';
import { DataHoraPipe } from '../../core/data-hora.pipe';
import { Pedido, StatusPedido } from '../../core/models';
import { PedidoService } from '../../core/pedido.service';
import { PASSOS_DO_PEDIDO, explicacaoDoStatus, nomeDoStatus, pedidoEncerrado } from '../../core/status-pedido';

const INTERVALO_DE_ATUALIZACAO = 15000; // 15 segundos

// Um passo da linha do tempo, ja com a situacao calculada.
interface Passo {
  status: StatusPedido;
  nome: string;
  situacao: 'feito' | 'atual' | 'pendente';
  hora: string | null;
}

// Acompanhamento do pedido (RF-007): status atual e a linha do tempo
// RECEBIDO > CONFIRMADO > EM_PREPARO > PRONTO > SAIU_ENTREGA.
// A tela consulta o back a cada 15 segundos para mostrar as mudancas da cozinha.
@Component({
  selector: 'app-pedido',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe],
  templateUrl: './pedido.html',
  styleUrl: './pedido.scss'
})
export class PedidoPagina {
  private rota = inject(ActivatedRoute);
  private pedidos = inject(PedidoService);

  private id = Number(this.rota.snapshot.paramMap.get('id'));
  // Veio direto do checkout: mostra a mensagem "Pedido confirmado!".
  protected novo = this.rota.snapshot.queryParamMap.get('novo') !== null;

  protected pedido = signal<Pedido | null>(null);
  protected carregando = signal(true);
  protected falhou = signal(false);

  protected nomeDoStatus = nomeDoStatus;

  protected explicacao = computed(() => {
    const pedido = this.pedido();
    return pedido ? explicacaoDoStatus(pedido.status) : '';
  });

  protected cancelado = computed(() => this.pedido()?.status === 'CANCELADO');

  protected passos = computed<Passo[]>(() => {
    const pedido = this.pedido();
    if (!pedido) {
      return [];
    }
    // Posicao do status atual na sequencia. Pedido entregue: todos os passos feitos.
    const posicaoAtual =
      pedido.status === 'FINALIZADO' ? PASSOS_DO_PEDIDO.length : PASSOS_DO_PEDIDO.indexOf(pedido.status);

    return PASSOS_DO_PEDIDO.map((status, posicao) => {
      const registro = pedido.historico.find((ponto) => ponto.status === status);
      const situacao: Passo['situacao'] =
        posicao < posicaoAtual ? 'feito' : posicao === posicaoAtual ? 'atual' : 'pendente';
      return { status, nome: nomeDoStatus(status), situacao, hora: registro?.dataHora ?? null };
    });
  });

  constructor() {
    this.pedidos.detalhe(this.id).subscribe({
      next: (pedido) => {
        this.pedido.set(pedido);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });

    // Atualizacao periodica. O DestroyRef desliga o relogio quando o usuario
    // sai da tela, para ele nao continuar rodando em segundo plano.
    const relogio = setInterval(() => this.atualizarStatus(), INTERVALO_DE_ATUALIZACAO);
    inject(DestroyRef).onDestroy(() => clearInterval(relogio));
  }

  private atualizarStatus(): void {
    const atual = this.pedido();
    if (!atual || pedidoEncerrado(atual.status)) {
      return;
    }
    this.pedidos.status(this.id).subscribe({
      next: (resposta) =>
        this.pedido.update((pedido) =>
          pedido
            ? {
                ...pedido,
                status: resposta.status,
                historico: resposta.historico,
                motivoCancelamento: resposta.motivoCancelamento,
                atualizadoEm: resposta.atualizadoEm
              }
            : pedido
        ),
      error: () => {
        // Falha passageira de rede: tenta de novo na proxima rodada.
      }
    });
  }
}
