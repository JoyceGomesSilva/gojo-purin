import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminPrato, AdminPratoRequest, FiltrosDePrato, Pagina } from './models';

// Conversa com /api/admin/pratos (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminPratoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/pratos`;

  // Lista paginada. Cada filtro so vai na chamada se estiver preenchido.
  listar(filtros: FiltrosDePrato, pagina: number, tamanho = 10): Observable<Pagina<AdminPrato>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.categoriaId) {
      params = params.set('categoriaId', filtros.categoriaId);
    }
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    if (filtros.busca.trim()) {
      params = params.set('busca', filtros.busca.trim());
    }
    return this.http.get<Pagina<AdminPrato>>(this.url, { params });
  }

  criar(dados: AdminPratoRequest): Observable<AdminPrato> {
    return this.http.post<AdminPrato>(this.url, dados);
  }

  editar(id: number, dados: AdminPratoRequest): Observable<AdminPrato> {
    return this.http.put<AdminPrato>(`${this.url}/${id}`, dados);
  }

  // O back nao apaga: marca como INATIVO. Responde 204, sem corpo.
  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
