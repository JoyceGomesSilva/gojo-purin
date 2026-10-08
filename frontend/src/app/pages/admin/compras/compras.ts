import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import {
  AdminCompraService,
  classeDoStatusDeCompra,
  nomeDoStatusDeCompra
} from '../../../core/admin-compra.service';
import { AdminFornecedorService } from '../../../core/admin-fornecedor.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DataHoraPipe } from '../../../core/data-hora.pipe';
import { Compra, FiltrosDeCompra, Fornecedor } from '../../../core/models';

const SEM_FILTROS: FiltrosDeCompra = { status: '', fornecedorId: '' };

// Lista dos pedidos de compra (RF-024), mais recentes primeiro.
@Component({
  selector: 'app-admin-compras',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe],
  templateUrl: './compras.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminCompras {
  private servico = inject(AdminCompraService);
  private fornecedorServico = inject(AdminFornecedorService);

  protected nomeDoStatus = nomeDoStatusDeCompra;
  protected classeDoStatus = classeDoStatusDeCompra;

  protected fornecedores = signal<Fornecedor[]>([]);
  protected filtros = signal<FiltrosDeCompra>(SEM_FILTROS);
  protected compras = signal<Compra[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected total = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  constructor() {
    this.fornecedorServico.opcoes().subscribe({
      next: (fornecedores) => this.fornecedores.set(fornecedores)
    });
    this.buscar();
  }

  filtrar(campo: keyof FiltrosDeCompra, evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.filtros.update((atuais) => ({ ...atuais, [campo]: valor }));
    this.pagina.set(0);
    this.buscar();
  }

  irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.buscar();
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.compras.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.total.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
