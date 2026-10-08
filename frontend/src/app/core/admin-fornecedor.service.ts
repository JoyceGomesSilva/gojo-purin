import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CatalogoItem,
  CatalogoItemRequest,
  Cotacao,
  FiltrosDeFornecedor,
  Fornecedor,
  FornecedorRequest,
  Pagina
} from './models';

// Conversa com /api/admin/fornecedores e /api/admin/cotacao (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminFornecedorService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/fornecedores`;

  // Lista paginada. Cada filtro so vai na chamada se estiver preenchido.
  listar(filtros: FiltrosDeFornecedor, pagina: number, tamanho = 10): Observable<Pagina<Fornecedor>> {
    let params = new HttpParams().set('page', pagina).set('size', tamanho);
    if (filtros.status) {
      params = params.set('status', filtros.status);
    }
    if (filtros.busca.trim()) {
      params = params.set('busca', filtros.busca.trim());
    }
    return this.http.get<Pagina<Fornecedor>>(this.url, { params });
  }

  // Os ativos, para campos de escolha.
  opcoes(): Observable<Fornecedor[]> {
    return this.http.get<Fornecedor[]>(`${this.url}/opcoes`);
  }

  detalhe(id: number): Observable<Fornecedor> {
    return this.http.get<Fornecedor>(`${this.url}/${id}`);
  }

  criar(dados: FornecedorRequest): Observable<Fornecedor> {
    return this.http.post<Fornecedor>(this.url, dados);
  }

  editar(id: number, dados: FornecedorRequest): Observable<Fornecedor> {
    return this.http.put<Fornecedor>(`${this.url}/${id}`, dados);
  }

  // O back nao apaga: marca como INATIVO. Responde 204, sem corpo.
  desativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  catalogo(fornecedorId: number): Observable<CatalogoItem[]> {
    return this.http.get<CatalogoItem[]>(`${this.url}/${fornecedorId}/catalogo`);
  }

  // Coloca o ingrediente no catalogo ou atualiza o preco dele.
  salvarNoCatalogo(fornecedorId: number, dados: CatalogoItemRequest): Observable<CatalogoItem> {
    return this.http.put<CatalogoItem>(`${this.url}/${fornecedorId}/catalogo`, dados);
  }

  // Cotacao comparativa de um ingrediente, com o historico de precos.
  cotacao(ingredienteId: number): Observable<Cotacao> {
    return this.http.get<Cotacao>(`${environment.apiUrl}/api/admin/cotacao/${ingredienteId}`);
  }
}
