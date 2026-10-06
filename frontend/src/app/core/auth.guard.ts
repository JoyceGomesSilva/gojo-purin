import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

// So deixa entrar na rota quem esta logado (RF-043).
// Quem nao esta vai para o login, e o endereco que tentou abrir segue em "voltar"
// para ser retomado depois de entrar.
export const authGuard: CanActivateFn = (_rota, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.logado()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { voltar: estado.url } });
};
