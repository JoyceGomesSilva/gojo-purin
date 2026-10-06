import { Component, DestroyRef, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AdminPedidoService } from '../../../core/admin-pedido.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DataHoraPipe } from '../../../core/data-hora.pipe';
import { AdminPedido, FiltrosDePedido, StatusPedido } from '../../../core/models';
import { CICLO_DO_PEDIDO, nomeDoStatus } from '../../../core/status-pedido';
import { AcoesPedido } from '../acoes-pedido/acoes-pedido';

const INTERVALO_DE_ATUALIZACAO = 20000; // 20 segundos
const SEM_FILTROS: FiltrosDePedido = { status: '', canal: '', de: '', ate: '' };

// Lista de pedidos do painel (RF-015 e RF-019): mais recentes primeiro,
// com filtros por status, canal e data, paginada. A lista se atualiza
// sozinha a cada 20 segundos para os pedidos novos aparecerem.
@Component({
  selector: 'app-admin-pedidos',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe, AcoesPedido],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.scss'
})
export class AdminPedidos {
  private servico = inject(AdminPedidoService);

  protected opcoesDeStatus: StatusPedido[] = [...CICLO_DO_PEDIDO, 'CANCELADO'];
  protected nomeDoStatus = nomeDoStatus;

  protected filtros = signal<FiltrosDePedido>(SEM_FILTROS);
  protected pedidos = signal<AdminPedido[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected totalPedidos = signal(0);

  protected carregando = signal(true);
  protected falhou = signal(false);

  constructor() {
    this.buscar();

    const relogio = setInterval(() => this.buscar(true), INTERVALO_DE_ATUALIZACAO);
    inject(DestroyRef).onDestroy(() => clearInterval(relogio));
  }

  // Chamado quando um campo de filtro muda. Volta para a primeira pagina.
  filtrar(campo: keyof FiltrosDePedido, evento: Event): void {
    const valor = (evento.target as HTMLInputElement | HTMLSelectElement).value;
    this.filtros.update((atuais) => ({ ...atuais, [campo]: valor }));
    this.pagina.set(0);
    this.buscar();
  }

  limparFiltros(): void {
    this.filtros.set(SEM_FILTROS);
    this.pagina.set(0);
    this.buscar();
  }

  irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.buscar();
  }

  // Um pedido mudou (status ou cancelamento): troca so ele na lista.
  trocar(novo: AdminPedido): void {
    this.pedidos.update((lista) => lista.map((pedido) => (pedido.id === novo.id ? novo : pedido)));
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  // silencioso = atualizacao automatica: nao mostra "carregando" nem erro,
  // para a tela nao piscar a cada rodada.
  private buscar(silencioso = false): void {
    if (!silencioso) {
      this.carregando.set(true);
      this.falhou.set(false);
    }

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.pedidos.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.totalPedidos.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        if (!silencioso) {
          this.falhou.set(true);
          this.carregando.set(false);
        }
      }
    });
  }
}
