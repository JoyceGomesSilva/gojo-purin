import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from './core/auth.service';
import { CarrinhoService } from './core/carrinho.service';

// A "moldura" do site: a faixa do topo e, embaixo, a tela da rota atual.
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, MatButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected auth = inject(AuthService);
  protected carrinho = inject(CarrinhoService);
  private router = inject(Router);

  sair(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
