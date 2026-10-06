import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Categoria, Combo, Pagina, PratoCardapio, PratoDetalhe } from './models';

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

  // Detalhe do prato, com os grupos de complementos.
  prato(id: number): Observable<PratoDetalhe> {
    return this.http.get<PratoDetalhe>(`${this.url}/${id}`);
  }

  combos(): Observable<Combo[]> {
    return this.http.get<Combo[]>(`${this.url}/combos`);
  }

  combo(id: number): Observable<Combo> {
    return this.http.get<Combo>(`${this.url}/combos/${id}`);
  }
}
