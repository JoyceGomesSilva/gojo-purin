import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService, mensagemDeErro } from '../../core/auth.service';

// Tela de cadastro de cliente (RF-004). O back sempre cria o perfil CLIENTE.
@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './cadastro.html',
  styleUrl: '../auth-form.scss'
})
export class Cadastro {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private rota = inject(ActivatedRoute);
  private aviso = inject(MatSnackBar);

  protected voltar = this.rota.snapshot.queryParamMap.get('voltar');

  protected carregando = signal(false);

  // As mesmas regras do RegisterRequest no back. Validar aqui evita uma ida ao
  // servidor, mas o back valida de novo, porque o front pode ser burlado.
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    senha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(60)]],
    telefone: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(20)]],
    endereco: ['', [Validators.required, Validators.maxLength(255)]]
  });

  cadastrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.carregando.set(true);
    this.auth.registrar(this.form.getRawValue()).subscribe({
      next: (sessao) => {
        // O cadastro ja devolve o token, entao a pessoa sai logada.
        this.aviso.open(`Conta criada. Bem-vindo(a), ${sessao.nome}!`, 'OK', { duration: 3000 });
        this.router.navigateByUrl(this.voltar ?? '/');
      },
      error: (erro: unknown) => {
        this.carregando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 5000 });
      }
    });
  }
}
