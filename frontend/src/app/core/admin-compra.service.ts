import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Compra, CompraRequest, FiltrosDeCompra, Pagina, RecebimentoRequest, StatusCompra } from './models';

const NOMES_DOS_STATUS: Record<StatusCompra, string> = {
  RASCUNHO: 'Rascunho',
  ENVIADO: 'Enviado',
  RECEBIDO: 'Recebido',
  CANCELADO: 'Cancelado'
};

export function nomeDoStatusDeCompra(status: StatusCompra): string {
  return NOMES_DOS_STATUS[status];
}

// A cor da etiqueta de cada status (classes do admin-crud.scss).
export function classeDoStatusDeCompra(status: StatusCompra): string {
  switch (status) {
    case 'RASCUNHO':
      return 'etiqueta aviso';
    case 'RECEBIDO':
      return 'etiqueta ativo';
    case 'CANCELADO':
      return 'etiqueta inativo';
    default:
      return 'etiqueta';
  }
}

// Conversa com /api/admin/compras (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminCompraService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/compras`;

  listar(filtros: FiltrosDeCompra, pagina: number, tamanho = 10): Observable<Pagina<Compra>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    if (filtros.fornecedorId) {
      params = params.set('fornecedorId', filtros.fornecedorId);
    }
    return this.http.get<Pagina<Compra>>(this.url, { params });
  }

  detalhe(id: number): Observable<Compra> {
    return this.http.get<Compra>(`${this.url}/${id}`);
  }

  criar(dados: CompraRequest): Observable<Compra> {
    return this.http.post<Compra>(this.url, dados);
  }

  editar(id: number, dados: CompraRequest): Observable<Compra> {
    return this.http.put<Compra>(`${this.url}/${id}`, dados);
  }

  // RASCUNHO > ENVIADO
  enviar(id: number): Observable<Compra> {
    return this.http.patch<Compra>(`${this.url}/${id}/enviar`, {});
  }

  // ENVIADO > RECEBIDO: o back da entrada no estoque e atualiza o custo.
  receber(id: number, dados: RecebimentoRequest): Observable<Compra> {
    return this.http.post<Compra>(`${this.url}/${id}/receber`, dados);
  }

  // O back nao apaga: marca como CANCELADO. Responde 204, sem corpo.
  cancelar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
