import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { FiltrosDeMovimentacao, Movimentacao, MovimentacaoRequest, Pagina, SaldoEstoque } from './models';

// Conversa com /api/admin/estoque (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminEstoqueService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/estoque`;

  // Quantos ingredientes estao abaixo do minimo. Fica aqui no servico (que e
  // um so para o site todo) porque duas telas usam: o menu do painel mostra
  // o selo e a tela de estoque atualiza o numero.
  private alertasAtuais = signal(0);
  readonly totalAlertas = this.alertasAtuais.asReadonly();

  // Saldo de todos os ingredientes ativos.
  saldos(): Observable<SaldoEstoque[]> {
    return this.http.get<SaldoEstoque[]>(`${this.url}/saldo`);
  }

  // So os que estao abaixo do minimo. Aproveita e atualiza o numero do selo.
  alertas(): Observable<SaldoEstoque[]> {
    return this.http
      .get<SaldoEstoque[]>(`${this.url}/alertas`)
      .pipe(tap((lista) => this.alertasAtuais.set(lista.length)));
  }

  // Para quem so quer o numero do selo atualizado (o menu do painel).
  atualizarAlertas(): void {
    this.alertas().subscribe({ error: () => this.alertasAtuais.set(0) });
  }

  // Entrada ou saida manual. O back devolve o saldo novo do ingrediente.
  movimentar(dados: MovimentacaoRequest): Observable<SaldoEstoque> {
    return this.http.post<SaldoEstoque>(`${this.url}/movimentacao`, dados);
  }

  // Historico paginado, do mais recente para o mais antigo.
  movimentacoes(filtros: FiltrosDeMovimentacao, pagina: number, tamanho = 20): Observable<Pagina<Movimentacao>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.ingredienteId) {
      params = params.set('ingredienteId', filtros.ingredienteId);
    }
    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }
    return this.http.get<Pagina<Movimentacao>>(`${this.url}/movimentacoes`, { params });
  }
}
