import { HttpInterceptorFn } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { environment } from '../../environments/environment';

// Quanto tempo uma chamada pode levar antes de mostrarmos o aviso.
const ESPERA_ANTES_DO_AVISO = 5000; // 5 segundos

// Guarda quantas chamadas a API estao demorando. O back fica no plano
// gratuito do Render, que "dorme" depois de um tempo parado e leva ate um
// minuto para acordar. Enquanto houver chamada lenta, o app mostra um aviso.
@Injectable({ providedIn: 'root' })
export class ServidorAcordando {
  private lentas = signal(0);

  // true enquanto pelo menos uma chamada estiver demorando.
  readonly acordando = computed(() => this.lentas() > 0);

  comecouADemorar(): void {
    this.lentas.update((total) => total + 1);
  }

  terminou(): void {
    this.lentas.update((total) => Math.max(0, total - 1));
  }
}

// Passa por todas as chamadas HTTP (como o authInterceptor). Para cada
// chamada a nossa API, liga um "relogio" de 5 segundos: se a resposta nao
// chegou ate la, conta como lenta. Quando a resposta chega (ou da erro),
// desliga o relogio e, se tinha contado, desconta.
export const servidorAcordandoInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  const aviso = inject(ServidorAcordando);
  let contou = false;
  const relogio = setTimeout(() => {
    contou = true;
    aviso.comecouADemorar();
  }, ESPERA_ANTES_DO_AVISO);

  // finalize roda no fim da chamada, deu certo ou deu erro.
  return next(req).pipe(
    finalize(() => {
      clearTimeout(relogio);
      if (contou) {
        aviso.terminou();
      }
    })
  );
};
