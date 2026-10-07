import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FichaTecnica, FichaTecnicaRequest, IngredienteOpcao } from './models';

// Conversa com a ficha tecnica de um prato: /api/admin/pratos/{id}/ficha (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminFichaService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin`;

  // Os ingredientes para o campo de escolha.
  ingredientes(): Observable<IngredienteOpcao[]> {
    return this.http.get<IngredienteOpcao[]>(`${this.url}/ingredientes/opcoes`);
  }

  // A ficha salva. Se o prato ainda nao tem, vem uma vazia.
  buscar(pratoId: number): Observable<FichaTecnica> {
    return this.http.get<FichaTecnica>(`${this.url}/pratos/${pratoId}/ficha`);
  }

  // Pede ao back as contas do que esta na tela, sem salvar.
  simular(pratoId: number, dados: FichaTecnicaRequest): Observable<FichaTecnica> {
    return this.http.post<FichaTecnica>(`${this.url}/pratos/${pratoId}/ficha/simular`, dados);
  }

  salvar(pratoId: number, dados: FichaTecnicaRequest): Observable<FichaTecnica> {
    return this.http.put<FichaTecnica>(`${this.url}/pratos/${pratoId}/ficha`, dados);
  }
}
