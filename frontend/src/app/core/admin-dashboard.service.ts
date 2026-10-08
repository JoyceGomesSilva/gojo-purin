import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardResumo, TopPrato, VendaDia } from './models';

// Conversa com /api/admin/dashboard (ADMIN e GERENTE).
@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/api/admin/dashboard`;

  // Os cards de hoje.
  resumo(): Observable<DashboardResumo> {
    return this.http.get<DashboardResumo>(`${this.url}/resumo`);
  }

  // Datas no formato "2026-10-08".
  topPratos(de: string, ate: string): Observable<TopPrato[]> {
    return this.http.get<TopPrato[]>(`${this.url}/top-pratos`, { params: new HttpParams().set('de', de).set('ate', ate) });
  }

  vendas(de: string, ate: string): Observable<VendaDia[]> {
    return this.http.get<VendaDia[]>(`${this.url}/vendas`, { params: new HttpParams().set('de', de).set('ate', ate) });
  }
}
