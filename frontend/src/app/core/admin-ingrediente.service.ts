import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FiltrosDeIngrediente, Ingrediente, IngredienteRequest, Pagina } from './models';

// Conversa com /api/admin/ingredientes (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminIngredienteService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/ingredientes`;

  // Lista paginada. Cada filtro so vai na chamada se estiver preenchido.
  listar(filtros: FiltrosDeIngrediente, pagina: number, tamanho = 10): Observable<Pagina<Ingrediente>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    if (filtros.busca.trim()) {
      params = params.set('busca', filtros.busca.trim());
    }
    return this.http.get<Pagina<Ingrediente>>(this.url, { params });
  }

  criar(dados: IngredienteRequest): Observable<Ingrediente> {
    return this.http.post<Ingrediente>(this.url, dados);
  }

  editar(id: number, dados: IngredienteRequest): Observable<Ingrediente> {
    return this.http.put<Ingrediente>(`${this.url}/${id}`, dados);
  }

  // O back nao apaga: marca como INATIVO. Responde 204, sem corpo.
  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
