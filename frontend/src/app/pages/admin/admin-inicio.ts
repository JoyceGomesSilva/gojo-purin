import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth.service';

// Tela provisoria do painel, so para testar os guards.
// Clientes e visitantes nao conseguem abrir /admin.
@Component({
  selector: 'app-admin-inicio',
  template: `
    <h1>Painel administrativo</h1>
    <p>Você entrou como {{ auth.perfil() }}.</p>
    <p>Dashboard, cardápio, pedidos e estoque entram aqui nas próximas etapas.</p>
  `
})
export class AdminInicio {
  protected auth = inject(AuthService);
}
