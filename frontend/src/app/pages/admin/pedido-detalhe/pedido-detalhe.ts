import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AdminPedidoService } from '../../../core/admin-pedido.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DataHoraPipe } from '../../../core/data-hora.pipe';
import { AdminPedido } from '../../../core/models';
import { nomeDoStatus } from '../../../core/status-pedido';
import { AcoesPedido } from '../acoes-pedido/acoes-pedido';

// Detalhe do pedido no painel (RF-020): itens, quantidades, observacoes,
// dados do cliente, valores e a linha do tempo de status.
@Component({
  selector: 'app-admin-pedido-detalhe',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe, AcoesPedido],
  templateUrl: './pedido-detalhe.html',
  styleUrl: './pedido-detalhe.scss'
})
export class AdminPedidoDetalhe {
  private rota = inject(ActivatedRoute);
  private servico = inject(AdminPedidoService);

  protected pedido = signal<AdminPedido | null>(null);
  protected carregando = signal(true);
  protected falhou = signal(false);

  protected nomeDoStatus = nomeDoStatus;

  constructor() {
    const id = Number(this.rota.snapshot.paramMap.get('id'));

    this.servico.detalhe(id).subscribe({
      next: (pedido) => {
        this.pedido.set(pedido);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  trocar(novo: AdminPedido): void {
    this.pedido.set(novo);
  }
}
