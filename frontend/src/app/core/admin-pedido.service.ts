import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminPedido, FiltrosDePedido, Pagina, StatusPedido } from './models';

// Conversa com /api/admin/pedidos (so a equipe interna tem acesso).
@Injectable({ providedIn: 'root' })
export class AdminPedidoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/pedidos`;

  // Lista paginada. Cada filtro so vai na chamada se estiver preenchido.
  listar(filtros: FiltrosDePedido, pagina: number, tamanho = 10): Observable<Pagina<AdminPedido>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    if (filtros.canal) {
      params = params.set('canal', filtros.canal);
    }
    if (filtros.de) {
      params = params.set('de', filtros.de);
    }
    if (filtros.ate) {
      params = params.set('ate', filtros.ate);
    }
    return this.http.get<Pagina<AdminPedido>>(this.url, { params });
  }

  detalhe(id: number): Observable<AdminPedido> {
    return this.http.get<AdminPedido>(`${this.url}/${id}`);
  }

  mudarStatus(id: number, status: StatusPedido): Observable<AdminPedido> {
    return this.http.patch<AdminPedido>(`${this.url}/${id}/status`, { status });
  }

  cancelar(id: number, motivo: string): Observable<AdminPedido> {
    return this.http.patch<AdminPedido>(`${this.url}/${id}/cancelar`, { motivo });
  }
}
