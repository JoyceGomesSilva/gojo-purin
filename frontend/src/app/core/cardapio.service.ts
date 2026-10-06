import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Categoria, Pagina, PratoCardapio } from './models';

// Conversa com /api/cardapio (endpoints publicos, sem login).
@Injectable({ providedIn: 'root' })
export class CardapioService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/cardapio`;

  categorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.url}/categorias`);
  }

  // categoriaId nulo traz o cardapio inteiro.
  pratos(categoriaId: number | null, pagina: number, tamanho = 12): Observable<Pagina<PratoCardapio>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (categoriaId !== null) {
      params = params.set('categoriaId', categoriaId);
    }
    return this.http.get<Pagina<PratoCardapio>>(this.url, { params });
  }

  prato(id: number): Observable<PratoCardapio> {
    return this.http.get<PratoCardapio>(`${this.url}/${id}`);
  }
}
