import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private http = inject(HttpClient);
  mensagem = signal('Conectando com a cozinha...');

  constructor() {
    this.http
      .get(`${environment.apiUrl}/api/ping`, { responseType: 'text' })
      .subscribe({
        next: (texto) => this.mensagem.set(texto),
        error: () => this.mensagem.set('Não consegui falar com o back-end.')
      });
  }
}