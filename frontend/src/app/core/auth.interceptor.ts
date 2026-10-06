import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

// Passa por todas as chamadas HTTP do Angular (RF-042):
// 1. coloca o token no cabecalho Authorization;
// 2. se o back responder 401 (token vencido ou invalido), desloga e manda para o login.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.token();
  const paraNossaApi = req.url.startsWith(environment.apiUrl);
  const rotaDeAuth = req.url.includes('/api/auth/');

  // A requisicao e imutavel: para mudar o cabecalho, criamos uma copia.
  const requisicao =
    token && paraNossaApi && !rotaDeAuth
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(requisicao).pipe(
    catchError((erro: unknown) => {
      if (erro instanceof HttpErrorResponse && erro.status === 401 && !rotaDeAuth) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { voltar: router.url } });
      }
      return throwError(() => erro);
    })
  );
};
