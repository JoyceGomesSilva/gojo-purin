import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth.service';

const NOMES_DOS_PERFIS: Record<string, string> = {
  ADMIN: 'Admin',
  GERENTE: 'Gerente',
  COZINHEIRO: 'Cozinheiro'
};

// A moldura do painel: o menu das secoes e, embaixo, a tela da secao aberta.
// As proximas secoes (cardapio, estoque, dashboard...) entram neste menu.
@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss'
})
export class AdminLayout {
  private auth = inject(AuthService);

  protected perfil = computed(() => NOMES_DOS_PERFIS[this.auth.perfil() ?? ''] ?? '');
  // Alguns itens do menu so aparecem para o admin.
  protected admin = computed(() => this.auth.perfil() === 'ADMIN');
}
