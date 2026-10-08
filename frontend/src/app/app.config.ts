import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth.interceptor';
import { servidorAcordandoInterceptor } from './core/servidor-acordando';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // Liga o HttpClient e registra os interceptors: o que envia o token e o
    // que percebe quando o servidor esta demorando para acordar.
    provideHttpClient(withInterceptors([authInterceptor, servidorAcordandoInterceptor]))
  ]
};
