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
    // Painel: a moldura (AdminLayout) fica fixa e as telas filhas trocam dentro dela.
    path: 'admin',
    canActivate: [authGuard, roleGuard('ADMIN', 'GERENTE', 'COZINHEIRO')],
    loadComponent: () => import('./pages/admin/admin-layout').then((m) => m.AdminLayout),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'pedidos' },
      {
        path: 'pedidos',
        loadComponent: () => import('./pages/admin/pedidos/pedidos').then((m) => m.AdminPedidos)
      },
      {
        path: 'pedidos/:id',
        loadComponent: () =>
          import('./pages/admin/pedido-detalhe/pedido-detalhe').then((m) => m.AdminPedidoDetalhe)
      },
      {
        // Cardapio: so ADMIN e GERENTE. O cozinheiro nao entra.
        path: 'pratos',
        canActivate: [roleGuard('ADMIN', 'GERENTE')],
        loadComponent: () => import('./pages/admin/pratos/pratos').then((m) => m.AdminPratos)
      },
      {
        // Ficha tecnica e custo de um prato (RF-011 a RF-013).
        path: 'pratos/:id/ficha',
        canActivate: [roleGuard('ADMIN', 'GERENTE')],
        loadComponent: () => import('./pages/admin/ficha/ficha').then((m) => m.AdminFicha)
      },
      {
        path: 'categorias',
        canActivate: [roleGuard('ADMIN', 'GERENTE')],
        loadComponent: () => import('./pages/admin/categorias/categorias').then((m) => m.AdminCategorias)
      },
      {
        // Dentro do painel, esta tela e so do ADMIN (RF-041).
        path: 'usuarios',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () => import('./pages/admin/usuarios/usuarios').then((m) => m.AdminUsuarios)
      }
    ]
  },
  // Qualquer endereco desconhecido volta para o inicio.
  { path: '**', redirectTo: '' }
];
