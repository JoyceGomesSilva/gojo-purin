import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminFornecedorService } from '../../../core/admin-fornecedor.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { formatarCnpj, normalizarCnpj, validadorDeCnpj } from '../../../core/cnpj';
import { FiltrosDeFornecedor, Fornecedor, FornecedorRequest } from '../../../core/models';

const SEM_FILTROS: FiltrosDeFornecedor = { status: '', busca: '' };

// Fornecedores (RF-021): criar, editar, listar e desativar.
// O catalogo de cada um fica na tela "Catalogo" (botao na lista).
@Component({
  selector: 'app-admin-fornecedores',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './fornecedores.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminFornecedores {
  private fb = inject(FormBuilder);
  private servico = inject(AdminFornecedorService);
  private aviso = inject(MatSnackBar);

  protected formatarCnpj = formatarCnpj;

  // ----- Lista -----
  protected filtros = signal<FiltrosDeFornecedor>(SEM_FILTROS);
  protected fornecedores = signal<Fornecedor[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected totalFornecedores = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // ----- Formulario -----
  protected formAberto = signal(false);
  protected editando = signal<Fornecedor | null>(null);
  protected enviando = signal(false);

  // As mesmas regras do FornecedorRequest no back.
  protected form = this.fb.nonNullable.group({
    razaoSocial: ['', [Validators.required, Validators.maxLength(160)]],
    cnpj: ['', [Validators.required, validadorDeCnpj]],
    telefone: ['', [Validators.maxLength(20)]],
    email: ['', [Validators.email, Validators.maxLength(160)]],
    categoriasProdutos: ['', [Validators.maxLength(255)]],
    status: ['ATIVO' as 'ATIVO' | 'INATIVO', [Validators.required]]
  });

  constructor() {
    this.buscar();
  }

  // ---------- Lista ----------

  filtrar(campo: keyof FiltrosDeFornecedor, evento: Event): void {
    const valor = (evento.target as HTMLInputElement | HTMLSelectElement).value;
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
    this.form.reset();
    this.abrirForm();
  }

  editar(fornecedor: Fornecedor): void {
    this.editando.set(fornecedor);
    this.form.reset({
      razaoSocial: fornecedor.razaoSocial,
      cnpj: formatarCnpj(fornecedor.cnpj),
      telefone: fornecedor.telefone ?? '',
      email: fornecedor.email ?? '',
      categoriasProdutos: fornecedor.categoriasProdutos ?? '',
      status: fornecedor.status
    });
    this.abrirForm();
  }

  fecharForm(): void {
    this.formAberto.set(false);
  }

  // Ao sair do campo, mostra o CNPJ ja pontuado.
  pontuarCnpj(): void {
    const controle = this.form.controls.cnpj;
    controle.setValue(formatarCnpj(controle.value));
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const dados: FornecedorRequest = {
      razaoSocial: valores.razaoSocial.trim(),
      cnpj: normalizarCnpj(valores.cnpj),
      telefone: valores.telefone.trim() || null,
      email: valores.email.trim() || null,
      categoriasProdutos: valores.categoriasProdutos.trim() || null,
      status: valores.status
    };

    const editando = this.editando();
    const chamada = editando ? this.servico.editar(editando.id, dados) : this.servico.criar(dados);

    this.enviando.set(true);
    chamada.subscribe({
      next: (fornecedor) => {
        this.enviando.set(false);
        this.formAberto.set(false);
        this.aviso.open(
          `${fornecedor.razaoSocial}: ${editando ? 'fornecedor atualizado' : 'fornecedor criado'}`,
          'OK',
          { duration: 3000 }
        );
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 409 quando o CNPJ ja existe; 400 quando o CNPJ e invalido.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  desativar(fornecedor: Fornecedor): void {
    this.enviando.set(true);
    this.servico.desativar(fornecedor.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.open(`${fornecedor.razaoSocial} foi desativado e saiu das cotações`, 'OK', { duration: 4000 });
        this.buscar();
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  private abrirForm(): void {
    this.formAberto.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.fornecedores.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.totalFornecedores.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
