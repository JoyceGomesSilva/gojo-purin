import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FiltrosDeUsuario, Pagina, UsuarioInterno, UsuarioInternoRequest } from './models';

// Conversa com /api/admin/usuarios (so o ADMIN tem acesso).
@Injectable({ providedIn: 'root' })
export class AdminUsuarioService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/usuarios`;

  // Lista paginada. Cada filtro so vai na chamada se estiver preenchido.
  listar(filtros: FiltrosDeUsuario, pagina: number, tamanho = 10): Observable<Pagina<UsuarioInterno>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.perfil) {
      params = params.set('perfil', filtros.perfil);
    }
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    return this.http.get<Pagina<UsuarioInterno>>(this.url, { params });
  }

  criar(dados: UsuarioInternoRequest): Observable<UsuarioInterno> {
    return this.http.post<UsuarioInterno>(this.url, dados);
  }

  editar(id: number, dados: UsuarioInternoRequest): Observable<UsuarioInterno> {
    return this.http.put<UsuarioInterno>(`${this.url}/${id}`, dados);
  }

  // O back nao apaga: marca como INATIVO. Responde 204, sem corpo (por isso void).
  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  reativar(id: number): Observable<UsuarioInterno> {
    return this.http.patch<UsuarioInterno>(`${this.url}/${id}/reativar`, {});
  }
}
