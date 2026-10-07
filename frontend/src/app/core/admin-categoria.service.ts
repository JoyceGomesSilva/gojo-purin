import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AdminCategoria, AdminCategoriaRequest } from './models';

// Conversa com /api/admin/categorias (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminCategoriaService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/categorias`;

  // Todas as categorias, ativas e inativas, na ordem do cardapio.
  listar(): Observable<AdminCategoria[]> {
    return this.http.get<AdminCategoria[]>(this.url);
  }

  criar(dados: AdminCategoriaRequest): Observable<AdminCategoria> {
    return this.http.post<AdminCategoria>(this.url, dados);
  }

  editar(id: number, dados: AdminCategoriaRequest): Observable<AdminCategoria> {
    return this.http.put<AdminCategoria>(`${this.url}/${id}`, dados);
  }

  // O back nao apaga: marca como INATIVO. Responde 204, sem corpo.
  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
