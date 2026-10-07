import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AdminEstoqueService } from '../../../core/admin-estoque.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DataHoraPipe } from '../../../core/data-hora.pipe';
import { FiltrosDeMovimentacao, Movimentacao, SaldoEstoque, TipoMovimentacao } from '../../../core/models';
import { formatarQuantidade } from '../../../core/quantidade';

const SEM_FILTROS: FiltrosDeMovimentacao = { ingredienteId: '', tipo: '' };

const NOMES_DOS_TIPOS: Record<TipoMovimentacao, string> = {
  ENTRADA: 'Entrada',
  SAIDA: 'Saída',
  ESTORNO: 'Estorno'
};

const NOMES_DOS_MOTIVOS: Record<string, string> = {
  COMPRA: 'Compra',
  VENDA: 'Venda',
  DESPERDICIO: 'Desperdício',
  VENCIMENTO: 'Vencimento',
  QUEBRA: 'Quebra',
  USO_INTERNO: 'Uso interno',
  AJUSTE: 'Ajuste',
  ESTORNO: 'Estorno'
};

// Historico do estoque (RF-033): todas as entradas e saidas, da mais recente
// para a mais antiga, com data, tipo, quantidade, motivo, usuario e referencia.
@Component({
  selector: 'app-admin-movimentacoes',
  imports: [RouterLink, MatButtonModule, BrlPipe, DataHoraPipe],
  templateUrl: './movimentacoes.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminMovimentacoes {
  private servico = inject(AdminEstoqueService);

  protected formatarQuantidade = formatarQuantidade;
  protected nomeDoTipo = (tipo: TipoMovimentacao) => NOMES_DOS_TIPOS[tipo];
  protected nomeDoMotivo = (motivo: string) => NOMES_DOS_MOTIVOS[motivo] ?? motivo;

  // Os ingredientes alimentam o filtro.
  protected ingredientes = signal<SaldoEstoque[]>([]);

  protected filtros = signal<FiltrosDeMovimentacao>(SEM_FILTROS);
  protected movimentacoes = signal<Movimentacao[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected total = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  constructor() {
    this.servico.saldos().subscribe({
      next: (saldos) => this.ingredientes.set(saldos),
      error: () => this.ingredientes.set([])
    });
    this.buscar();
  }

  // Chamado quando um filtro muda. Volta para a primeira pagina.
  filtrar(campo: keyof FiltrosDeMovimentacao, evento: Event): void {
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

    this.servico.movimentacoes(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.movimentacoes.set(resposta.conteudo);
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
