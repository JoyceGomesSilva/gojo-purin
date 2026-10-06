import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

// Cada rota carrega sua tela so quando for aberta (loadComponent),
// o que deixa o primeiro carregamento do site mais leve.
export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/inicio/inicio').then((m) => m.Inicio)
  },
  {
    path: 'prato/:id',
    loadComponent: () => import('./pages/prato/prato').then((m) => m.PratoDetalhe)
  },
  {
    path: 'combo/:id',
    loadComponent: () => import('./pages/combo/combo').then((m) => m.ComboPagina)
  },
  {
    path: 'carrinho',
    loadComponent: () => import('./pages/carrinho/carrinho').then((m) => m.Carrinho)
  },
  {
    // So clientes logados fecham pedido.
    path: 'checkout',
    canActivate: [authGuard, roleGuard('CLIENTE')],
    loadComponent: () => import('./pages/checkout/checkout').then((m) => m.Checkout)
  },
  {
    path: 'pedido/:id',
    canActivate: [authGuard, roleGuard('CLIENTE')],
    loadComponent: () => import('./pages/pedido/pedido').then((m) => m.PedidoPagina)
  },
  {
    path: 'meus-pedidos',
    canActivate: [authGuard, roleGuard('CLIENTE')],
    loadComponent: () => import('./pages/meus-pedidos/meus-pedidos').then((m) => m.MeusPedidos)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then((m) => m.Login)
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./pages/cadastro/cadastro').then((m) => m.Cadastro)
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN', 'GERENTE', 'COZINHEIRO')],
    loadComponent: () => import('./pages/admin/admin-inicio').then((m) => m.AdminInicio)
  },
  // Qualquer endereco desconhecido volta para o inicio.
  { path: '**', redirectTo: '' }
];
