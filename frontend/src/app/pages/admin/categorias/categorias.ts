import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminCategoriaService } from '../../../core/admin-categoria.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { AdminCategoria, AdminCategoriaRequest } from '../../../core/models';

// Categorias do cardapio (RF-009): criar, editar, listar e desativar.
// O mesmo formulario serve para criar e para editar.
@Component({
  selector: 'app-admin-categorias',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './categorias.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminCategorias {
  private fb = inject(FormBuilder);
  private servico = inject(AdminCategoriaService);
  private aviso = inject(MatSnackBar);

  // ----- Lista -----
  protected categorias = signal<AdminCategoria[]>([]);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // ----- Formulario -----
  protected formAberto = signal(false);
  // Qual categoria esta sendo editada. null = estamos criando uma nova.
  protected editando = signal<AdminCategoria | null>(null);
  protected enviando = signal(false);

  // As mesmas regras do AdminCategoriaRequest no back.
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    descricao: ['', [Validators.maxLength(255)]],
    ordem: [1 as number | null, [Validators.required, Validators.min(0), Validators.max(999)]],
    status: ['ATIVO' as 'ATIVO' | 'INATIVO', [Validators.required]]
  });

  constructor() {
    this.buscar();
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  nova(): void {
    this.editando.set(null);
    // Sugere a proxima posicao livre: a maior ordem que existe, mais 1.
    const maiorOrdem = Math.max(0, ...this.categorias().map((categoria) => categoria.ordem));
    this.form.reset({ nome: '', descricao: '', ordem: maiorOrdem + 1, status: 'ATIVO' });
    this.abrirForm();
  }

  editar(categoria: AdminCategoria): void {
    this.editando.set(categoria);
    this.form.reset({
      nome: categoria.nome,
      descricao: categoria.descricao ?? '',
      ordem: categoria.ordem,
      status: categoria.status
    });
    this.abrirForm();
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
    const dados: AdminCategoriaRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || null,
      ordem: valores.ordem ?? 0,
      status: valores.status
    };

    const editando = this.editando();
    const chamada = editando ? this.servico.editar(editando.id, dados) : this.servico.criar(dados);

    this.enviando.set(true);
    chamada.subscribe({
      next: (categoria) => {
        this.enviando.set(false);
        this.formAberto.set(false);
        this.aviso.open(`${categoria.nome}: ${editando ? 'categoria atualizada' : 'categoria criada'}`, 'OK', {
          duration: 3000
        });
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 409 quando ja existe categoria com o mesmo nome.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  desativar(categoria: AdminCategoria): void {
    this.enviando.set(true);
    this.servico.desativar(categoria.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.open(`${categoria.nome} foi desativada e saiu do cardápio com os pratos dela`, 'OK', {
          duration: 5000
        });
        this.buscar();
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  // Mostra o formulario e sobe a tela ate ele (fica no topo da pagina).
  private abrirForm(): void {
    this.formAberto.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.servico.listar().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
