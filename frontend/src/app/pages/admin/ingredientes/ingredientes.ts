import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminIngredienteService } from '../../../core/admin-ingrediente.service';
import { mensagemDeErro } from '../../../core/auth.service';
import {
  FiltrosDeIngrediente,
  Ingrediente,
  IngredienteRequest,
  UnidadeIngrediente
} from '../../../core/models';
import { formatarCustoUnitario, formatarQuantidade } from '../../../core/quantidade';

const SEM_FILTROS: FiltrosDeIngrediente = { status: '', busca: '' };

// Ingredientes do estoque (RF-027): criar, editar, listar e desativar.
// O mesmo formulario serve para criar e para editar.
@Component({
  selector: 'app-admin-ingredientes',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './ingredientes.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminIngredientes {
  private fb = inject(FormBuilder);
  private servico = inject(AdminIngredienteService);
  private aviso = inject(MatSnackBar);

  protected formatarQuantidade = formatarQuantidade;
  protected formatarCustoUnitario = formatarCustoUnitario;

  // ----- Lista -----
  protected filtros = signal<FiltrosDeIngrediente>(SEM_FILTROS);
  protected ingredientes = signal<Ingrediente[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected totalIngredientes = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // ----- Formulario -----
  protected formAberto = signal(false);
  // Qual ingrediente esta sendo editado. null = estamos criando um novo.
  protected editando = signal<Ingrediente | null>(null);
  protected enviando = signal(false);

  // As mesmas regras do IngredienteRequest no back.
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    sku: ['', [Validators.required, Validators.maxLength(30)]],
    unidadePadrao: ['G' as UnidadeIngrediente, [Validators.required]],
    estoqueMinimo: [0 as number | null, [Validators.required, Validators.min(0)]],
    custoUnitario: [0 as number | null, [Validators.required, Validators.min(0)]],
    status: ['ATIVO' as 'ATIVO' | 'INATIVO', [Validators.required]]
  });

  constructor() {
    this.buscar();
  }

  // ---------- Lista ----------

  // Chamado quando um filtro muda. Volta para a primeira pagina.
  filtrar(campo: keyof FiltrosDeIngrediente, evento: Event): void {
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
    this.form.reset({ nome: '', sku: '', unidadePadrao: 'G', estoqueMinimo: 0, custoUnitario: 0, status: 'ATIVO' });
    // Ao criar, a unidade pode ser escolhida.
    this.form.controls.unidadePadrao.enable();
    this.abrirForm();
  }

  editar(ingrediente: Ingrediente): void {
    this.editando.set(ingrediente);
    this.form.reset({
      nome: ingrediente.nome,
      sku: ingrediente.sku,
      unidadePadrao: ingrediente.unidadePadrao,
      estoqueMinimo: ingrediente.estoqueMinimo,
      custoUnitario: ingrediente.custoUnitario,
      status: ingrediente.status
    });
    // Depois de criado, a unidade fica travada: o saldo, as fichas tecnicas e
    // o custo estao todos nela. O back tambem recusa a troca.
    this.form.controls.unidadePadrao.disable();
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

    // getRawValue traz tambem os campos travados (a unidade, na edicao).
    const valores = this.form.getRawValue();
    const dados: IngredienteRequest = {
      nome: valores.nome.trim(),
      sku: valores.sku.trim().toUpperCase(),
      unidadePadrao: valores.unidadePadrao,
      estoqueMinimo: valores.estoqueMinimo ?? 0,
      custoUnitario: valores.custoUnitario ?? 0,
      status: valores.status
    };

    const editando = this.editando();
    const chamada = editando ? this.servico.editar(editando.id, dados) : this.servico.criar(dados);

    this.enviando.set(true);
    chamada.subscribe({
      next: (ingrediente) => {
        this.enviando.set(false);
        this.formAberto.set(false);
        this.aviso.open(
          `${ingrediente.nome}: ${editando ? 'ingrediente atualizado' : 'ingrediente criado'}`,
          'OK',
          { duration: 3000 }
        );
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 409 quando o SKU ja existe; 422 ao desativar ingrediente em uso.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  desativar(ingrediente: Ingrediente): void {
    this.enviando.set(true);
    this.servico.desativar(ingrediente.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.open(`${ingrediente.nome} foi desativado`, 'OK', { duration: 4000 });
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 422 quando o ingrediente esta na ficha de pratos ativos.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 10000 });
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

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.ingredientes.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.totalIngredientes.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
