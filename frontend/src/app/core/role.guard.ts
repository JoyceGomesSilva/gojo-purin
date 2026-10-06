import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Perfil } from './models';

// So deixa entrar quem tem um dos perfis informados (RF-043).
// Uso nas rotas: canActivate: [authGuard, roleGuard('ADMIN', 'GERENTE')]
export function roleGuard(...perfisPermitidos: Perfil[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    const perfil = auth.perfil();
    if (perfil && perfisPermitidos.includes(perfil)) {
      return true;
    }
    // Logado, mas sem permissao: volta para o inicio.
    return router.createUrlTree(['/']);
  };
}
