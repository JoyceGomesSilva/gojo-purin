import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { BrlPipe } from '../../core/brl.pipe';
import { DataHoraPipe } from '../../core/data-hora.pipe';
import { Pedido } from '../../core/models';
import { PedidoService } from '../../core/pedido.service';
import { nomeDoStatus } from '../../core/status-pedido';

// Historico do cliente (RF-008): data, itens, valor total e status,
// do pedido mais recente para o mais antigo.
@Component({
  selector: 'app-meus-pedidos',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe],
  templateUrl: './meus-pedidos.html',
  styleUrl: './meus-pedidos.scss'
})
export class MeusPedidos {
  private pedidoService = inject(PedidoService);

  protected pedidos = signal<Pedido[]>([]);
  protected carregando = signal(true);
  protected falhou = signal(false);
  protected temMais = signal(false);
  private pagina = 0;

  protected nomeDoStatus = nomeDoStatus;

  constructor() {
    this.buscar();
  }

  // "2 × Ramen Ichiraku, 1 × Six Eyes"
  protected resumoDosItens(pedido: Pedido): string {
    return pedido.itens.map((item) => `${item.quantidade} × ${item.pratoNome}`).join(', ');
  }

  verMais(): void {
    this.pagina++;
    this.buscar();
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.pedidoService.meus(this.pagina).subscribe({
      next: (resposta) => {
        this.pedidos.update((atuais) => (resposta.pagina === 0 ? resposta.conteudo : [...atuais, ...resposta.conteudo]));
        this.temMais.set(!resposta.ultima);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
