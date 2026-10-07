import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AdminEstoqueService } from '../../core/admin-estoque.service';
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
  private estoque = inject(AdminEstoqueService);

  protected perfil = computed(() => NOMES_DOS_PERFIS[this.auth.perfil() ?? ''] ?? '');
  // Alguns itens do menu so aparecem para o admin; outros, para admin e gerente.
  protected gestao = computed(() => this.auth.perfil() === 'ADMIN' || this.auth.perfil() === 'GERENTE');
  protected admin = computed(() => this.auth.perfil() === 'ADMIN');

  // RF-032: quantos ingredientes estao abaixo do minimo, para o selo vermelho do menu.
  // O numero mora no servico, e a tela de estoque o atualiza depois de cada movimentacao.
  protected totalAlertas = this.estoque.totalAlertas;

  constructor() {
    // O cozinheiro nao tem acesso ao estoque, entao nem pergunta.
    if (this.gestao()) {
      this.estoque.atualizarAlertas();
    }
  }
}
