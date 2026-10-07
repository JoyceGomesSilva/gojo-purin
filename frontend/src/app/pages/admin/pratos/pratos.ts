import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminCategoriaService } from '../../../core/admin-categoria.service';
import { AdminPratoService } from '../../../core/admin-prato.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { AdminCategoria, AdminPrato, AdminPratoRequest, FiltrosDePrato, StatusPrato } from '../../../core/models';

const SEM_FILTROS: FiltrosDePrato = { categoriaId: '', status: '', busca: '' };

const NOMES_DOS_STATUS: Record<StatusPrato, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  PAUSADO: 'Pausado'
};

// Formulario em branco. O prato novo comeca INATIVO: so pode ser ativado
// depois de ter ficha tecnica (RN01).
const PRATO_EM_BRANCO = {
  nome: '',
  descricao: '',
  categoriaId: null,
  precoVenda: null,
  tempoPreparoMin: 15,
  status: 'INATIVO' as StatusPrato,
  fotoUrl: '',
  anime: '',
  personagem: '',
  modoPreparo: ''
};

// Pratos do cardapio (RF-010 e RF-014): criar, editar, listar e desativar.
// O mesmo formulario serve para criar e para editar.
@Component({
  selector: 'app-admin-pratos',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, BrlPipe],
  templateUrl: './pratos.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminPratos {
  private fb = inject(FormBuilder);
  private servico = inject(AdminPratoService);
  private categoriaServico = inject(AdminCategoriaService);
  private aviso = inject(MatSnackBar);

  protected nomeDoStatus = (status: StatusPrato) => NOMES_DOS_STATUS[status];

  // As categorias alimentam o filtro e o campo "Categoria" do formulario.
  protected categorias = signal<AdminCategoria[]>([]);

  // ----- Lista -----
  protected filtros = signal<FiltrosDePrato>(SEM_FILTROS);
  protected pratos = signal<AdminPrato[]>([]);
  protected pagina = signal(0);
  protected totalPaginas = signal(0);
  protected totalPratos = signal(0);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // ----- Formulario -----
  protected formAberto = signal(false);
  // Qual prato esta sendo editado. null = estamos criando um novo.
  protected editando = signal<AdminPrato | null>(null);
  protected enviando = signal(false);

  // RN01: so da para escolher "Ativo" se o prato ja tem ficha tecnica.
  protected podeAtivar = computed(() => this.editando()?.temFicha ?? false);

  // As mesmas regras do AdminPratoRequest no back.
  protected form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    descricao: ['', [Validators.maxLength(500)]],
    categoriaId: [null as number | null, [Validators.required]],
    precoVenda: [null as number | null, [Validators.required, Validators.min(0.01)]],
    tempoPreparoMin: [15 as number | null, [Validators.required, Validators.min(1), Validators.max(600)]],
    status: ['INATIVO' as StatusPrato, [Validators.required]],
    fotoUrl: ['', [Validators.maxLength(500), Validators.pattern(/^(https?:\/\/.+)?$/)]],
    anime: ['', [Validators.maxLength(80)]],
    personagem: ['', [Validators.maxLength(80)]],
    modoPreparo: ['', [Validators.maxLength(5000)]]
  });

  constructor() {
    this.categoriaServico.listar().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: () => this.categorias.set([])
    });
    this.buscar();
  }

  // ---------- Lista ----------

  // Chamado quando um filtro muda. Volta para a primeira pagina.
  filtrar(campo: keyof FiltrosDePrato, evento: Event): void {
    const valor = (evento.target as HTMLInputElement | HTMLSelectElement).value;
    this.filtros.update((atuais) => ({ ...atuais, [campo]: valor }));
    this.pagina.set(0);
    this.buscar();
  }

  limparFiltros(): void {
    this.filtros.set(SEM_FILTROS);
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
    this.form.reset(PRATO_EM_BRANCO);
    this.abrirForm();
  }

  editar(prato: AdminPrato): void {
    this.editando.set(prato);
    // Os campos opcionais vem como null do back; no formulario viram texto vazio.
    this.form.reset({
      nome: prato.nome,
      descricao: prato.descricao ?? '',
      categoriaId: prato.categoriaId,
      precoVenda: prato.precoVenda,
      tempoPreparoMin: prato.tempoPreparoMin,
      status: prato.status,
      fotoUrl: prato.fotoUrl ?? '',
      anime: prato.anime ?? '',
      personagem: prato.personagem ?? '',
      modoPreparo: prato.modoPreparo ?? ''
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

    // "|| null": texto vazio vai como null. "?? 0": so para o TypeScript,
    // porque o formulario valido garante que os numeros estao preenchidos.
    const valores = this.form.getRawValue();
    const dados: AdminPratoRequest = {
      nome: valores.nome.trim(),
      descricao: valores.descricao.trim() || null,
      categoriaId: valores.categoriaId ?? 0,
      precoVenda: valores.precoVenda ?? 0,
      tempoPreparoMin: valores.tempoPreparoMin ?? 0,
      status: valores.status,
      fotoUrl: valores.fotoUrl.trim() || null,
      anime: valores.anime.trim() || null,
      personagem: valores.personagem.trim() || null,
      modoPreparo: valores.modoPreparo.trim() || null
    };

    const editando = this.editando();
    const chamada = editando ? this.servico.editar(editando.id, dados) : this.servico.criar(dados);

    this.enviando.set(true);
    chamada.subscribe({
      next: (prato) => {
        this.enviando.set(false);
        this.formAberto.set(false);
        this.aviso.open(`${prato.nome}: ${editando ? 'prato atualizado' : 'prato criado'}`, 'OK', { duration: 3000 });
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 422 ao tentar ativar um prato sem ficha tecnica.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  desativar(prato: AdminPrato): void {
    this.enviando.set(true);
    this.servico.desativar(prato.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.open(`${prato.nome} foi desativado e saiu do cardápio`, 'OK', { duration: 4000 });
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

    this.servico.listar(this.filtros(), this.pagina()).subscribe({
      next: (resposta) => {
        this.pratos.set(resposta.conteudo);
        this.totalPaginas.set(resposta.totalPaginas);
        this.totalPratos.set(resposta.totalElementos);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
