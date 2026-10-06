import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Pagina, Pedido, PedidoRequest, PedidoStatus } from './models';

// Conversa com /api/pedidos. Todas as chamadas exigem login de cliente;
// o token vai sozinho no cabecalho, colocado pelo interceptor.
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/pedidos`;

  criar(pedido: PedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.url, pedido);
  }

  meus(pagina: number, tamanho = 10): Observable<Pagina<Pedido>> {
    const params = new HttpParams().set('page', pagina).set('size', tamanho);
    return this.http.get<Pagina<Pedido>>(`${this.url}/meus`, { params });
  }

  detalhe(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.url}/${id}`);
  }

  status(id: number): Observable<PedidoStatus> {
    return this.http.get<PedidoStatus>(`${this.url}/${id}/status`);
  }

  enderecoCadastrado(): Observable<{ endereco: string | null }> {
    return this.http.get<{ endereco: string | null }>(`${this.url}/endereco-cadastrado`);
  }
}
