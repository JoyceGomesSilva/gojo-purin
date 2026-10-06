import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, mensagemDeErro } from '../../core/auth.service';

// Tela de login (RF-005 e RF-038). Serve para clientes e para a equipe.
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './login.html',
  styleUrl: '../auth-form.scss'
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private rota = inject(ActivatedRoute);
  private aviso = inject(MatSnackBar);

  // Para onde ir depois de entrar (ex.: /checkout, quando veio do carrinho).
  protected voltar = this.rota.snapshot.queryParamMap.get('voltar');

  protected carregando = signal(false);

  // Reactive Form: os campos e as regras de validacao ficam aqui no codigo.
  protected form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]]
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (sessao) => {
        this.aviso.open(`Bem-vindo(a), ${sessao.nome}!`, 'OK', { duration: 3000 });
        // Cliente volta para o site; equipe vai para o painel.
        const destino = this.voltar ?? (sessao.perfil === 'CLIENTE' ? '/' : '/admin');
        this.router.navigateByUrl(destino);
      },
      error: (erro: unknown) => {
        this.carregando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 5000 });
      }
    });
  }
}
