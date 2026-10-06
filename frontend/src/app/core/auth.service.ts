import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ErroApi, LoginRequest, Perfil, RegisterRequest, Sessao } from './models';

const CHAVE = 'gojo_purin_sessao';

// Guarda quem esta logado e conversa com /api/auth no back.
// A sessao fica no localStorage para o usuario continuar logado ao recarregar a pagina.
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  private sessaoAtual = signal<Sessao | null>(lerSessaoSalva());

  // Valores que as telas e os guards consultam.
  readonly sessao = this.sessaoAtual.asReadonly();
  readonly logado = computed(() => this.sessaoAtual() !== null);
  readonly perfil = computed<Perfil | null>(() => this.sessaoAtual()?.perfil ?? null);
  readonly equipe = computed(() => {
    const perfil = this.perfil();
    return perfil !== null && perfil !== 'CLIENTE';
  });

  login(dados: LoginRequest): Observable<Sessao> {
    return this.http
      .post<Sessao>(`${environment.apiUrl}/api/auth/login`, dados)
      .pipe(tap((sessao) => this.guardar(sessao)));
  }

  registrar(dados: RegisterRequest): Observable<Sessao> {
    return this.http
      .post<Sessao>(`${environment.apiUrl}/api/auth/register`, dados)
      .pipe(tap((sessao) => this.guardar(sessao)));
  }

  logout(): void {
    localStorage.removeItem(CHAVE);
    this.sessaoAtual.set(null);
  }

  token(): string | null {
    return this.sessaoAtual()?.token ?? null;
  }

  private guardar(sessao: Sessao): void {
    localStorage.setItem(CHAVE, JSON.stringify(sessao));
    this.sessaoAtual.set(sessao);
  }
}

// Le a sessao salva no navegador. Se o token ja venceu, descarta.
function lerSessaoSalva(): Sessao | null {
  try {
    const texto = localStorage.getItem(CHAVE);
    if (!texto) {
      return null;
    }
    const sessao = JSON.parse(texto) as Sessao;
    if (tokenVencido(sessao.token)) {
      localStorage.removeItem(CHAVE);
      return null;
    }
    return sessao;
  } catch {
    return null;
  }
}

// O JWT tem tres partes separadas por ponto. A do meio traz os dados,
// incluindo "exp": o momento em que o token vence (em segundos).
function tokenVencido(token: string): boolean {
  try {
    const meio = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const dados = JSON.parse(atob(meio)) as { exp?: number };
    return !dados.exp || dados.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

// Transforma um erro do HttpClient em uma frase para mostrar ao usuario.
export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof HttpErrorResponse) {
    if (erro.status === 0) {
      return 'Não consegui falar com o servidor. Tente de novo em instantes.';
    }
    const corpo = erro.error as Partial<ErroApi> | null;
    if (corpo?.detalhes?.length) {
      return corpo.detalhes.join(' · ');
    }
    if (corpo?.mensagem) {
      return corpo.mensagem;
    }
  }
  return 'Algo deu errado. Tente novamente.';
}
