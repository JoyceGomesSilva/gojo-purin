import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { AuthService } from './core/auth.service';

// A "moldura" do site: a barra do topo e, embaixo, a tela da rota atual.
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected auth = inject(AuthService);
  private router = inject(Router);

  sair(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
