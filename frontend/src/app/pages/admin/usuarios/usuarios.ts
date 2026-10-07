import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminUsuarioService } from '../../../core/admin-usuario.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { FiltrosDeUsuario, PerfilInterno, UsuarioInterno, UsuarioInternoRequest } from '../../../core/models';

const SEM_FILTROS: FiltrosDeUsuario = { perfil: '', status: '' };

const NOMES_DOS_PERFIS: Record<PerfilInterno, string> = {
  GERENTE: 'Gerente',
  COZINHEIRO: 'Cozinheiro'
};

// As regras da senha. Ao criar ela e obrigatoria; ao editar, pode ficar vazia.
const SENHA_AO_CRIAR = [Validators.required, Validators.minLength(6), Validators.maxLength(60)];
const SENHA_AO_EDITAR = [Validators.minLength(6), Validators.maxLength(60)];

// Usuarios internos (RF-041): o admin cria, edita, desativa e reativa
// gerentes e cozinheiros. O mesmo formulario serve para criar e para editar.
@Component({
  selector: 'app-admin-usuarios',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss'
})
export class AdminUsuarios {
  private fb = inject(FormBuilder);
  private servico = inject(AdminUsuarioService);
  private aviso = inject(MatSnackBar);

  protected nomeDoPerfil = (perfil: PerfilInterno) => NOMES_DOS_PERFIS[perfil];

  // ----- Lista -----
  protected filtros = signal<FiltrosDeUsuario>(SEM_FILTROS);
  protected usuarios = signal<UsuarioInterno[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected totalUsuarios = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // ----- Formulario -----
  protected formAberto = signal(false);
  // Quem esta sendo editado. null = estamos criando um usuario novo.
  protected editando = signal<UsuarioInterno | null>(null);
  protected enviando = signal(false);

  // As mesmas regras dos DTOs do back. O back valida de novo.
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(160)]],
    telefone: ['', [Validators.maxLength(20)]],
    perfil: ['COZINHEIRO' as PerfilInterno, [Validators.required]],
    senha: ['', SENHA_AO_CRIAR]
  });

  constructor() {
    this.buscar();
  }

  // ---------- Lista ----------

  // Chamado quando um filtro muda. Volta para a primeira pagina.
  filtrar(campo: keyof FiltrosDeUsuario, evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.filtros.update((atuais) => ({ ...atuais, [campo]: valor }));
    this.pagina.set(0);
    this.buscar();
  }

  irParaPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.buscar();
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  // ---------- Formulario ----------

  novo(): void {
    this.editando.set(null);
    this.form.reset(); // volta aos valores iniciais (perfil COZINHEIRO, resto vazio)
    this.ajustarRegrasDaSenha(SENHA_AO_CRIAR);
    this.formAberto.set(true);
  }

  editar(usuario: UsuarioInterno): void {
    this.editando.set(usuario);
    // Preenche o formulario com os dados atuais. A senha fica vazia de proposito:
    // o back nunca devolve a senha, e vazia significa "nao mexer".
    this.form.reset({
      nome: usuario.nome,
      email: usuario.email,
      telefone: usuario.telefone ?? '',
      perfil: usuario.perfil,
      senha: ''
    });
    this.ajustarRegrasDaSenha(SENHA_AO_EDITAR);
    this.formAberto.set(true);
  }

  fecharForm(): void {
    this.formAberto.set(false);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const dados: UsuarioInternoRequest = {
      nome: valores.nome.trim(),
      email: valores.email.trim(),
      perfil: valores.perfil,
      telefone: valores.telefone.trim() || null,
      senha: valores.senha || null
    };

    const editando = this.editando();
    const chamada = editando ? this.servico.editar(editando.id, dados) : this.servico.criar(dados);

    this.enviando.set(true);
    chamada.subscribe({
      next: (usuario) => {
        this.enviando.set(false);
        this.formAberto.set(false);
        this.aviso.open(`${usuario.nome}: ${editando ? 'dados atualizados' : 'usuário criado'}`, 'OK', {
          duration: 3000
        });
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 409 quando o e-mail ja e de outra conta.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  // ---------- Desativar e reativar ----------

  desativar(usuario: UsuarioInterno): void {
    this.enviando.set(true);
    this.servico.desativar(usuario.id).subscribe({
      next: () => this.depoisDeMudarStatus(`${usuario.nome} foi desativado(a) e não consegue mais entrar`),
      error: (erro: unknown) => this.mostrarErro(erro)
    });
  }

  reativar(usuario: UsuarioInterno): void {
    this.enviando.set(true);
    this.servico.reativar(usuario.id).subscribe({
      next: () => this.depoisDeMudarStatus(`${usuario.nome} foi reativado(a)`),
      error: (erro: unknown) => this.mostrarErro(erro)
    });
  }

  private depoisDeMudarStatus(mensagem: string): void {
    this.enviando.set(false);
    this.aviso.open(mensagem, 'OK', { duration: 4000 });
    this.buscar();
  }

  private mostrarErro(erro: unknown): void {
    this.enviando.set(false);
    this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
  }

  // Troca as regras do campo senha e manda o Angular reavaliar o campo.
  private ajustarRegrasDaSenha(regras: typeof SENHA_AO_CRIAR): void {
    this.form.controls.senha.setValidators(regras);
    this.form.controls.senha.updateValueAndValidity();
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.usuarios.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.totalUsuarios.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
