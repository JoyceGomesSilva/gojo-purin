import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { AdminFichaService } from '../../../core/admin-ficha.service';
import { AdminFornecedorService } from '../../../core/admin-fornecedor.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { formatarCnpj } from '../../../core/cnpj';
import { CatalogoItem, Fornecedor, IngredienteOpcao } from '../../../core/models';
import { formatarCustoUnitario } from '../../../core/quantidade';

// Catalogo de um fornecedor (RF-022): quais ingredientes ele vende e por
// quanto. Cada mudanca de preco entra no historico (RF-026).
@Component({
  selector: 'app-admin-catalogo',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './catalogo.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminCatalogo {
  private servico = inject(AdminFornecedorService);
  private fichaServico = inject(AdminFichaService);
  private aviso = inject(MatSnackBar);

  private fornecedorId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected formatarCnpj = formatarCnpj;
  protected formatarCustoUnitario = formatarCustoUnitario;

  protected fornecedor = signal<Fornecedor | null>(null);
  protected itens = signal<CatalogoItem[]>([]);
  protected ingredientes = signal<IngredienteOpcao[]>([]);
  protected carregando = signal(true);
  protected falhou = signal(false);
  protected enviando = signal(false);

  // ----- Formulario: adicionar ingrediente ou mudar o preco -----
  protected form = new FormGroup({
    ingredienteId: new FormControl<number | null>(null, [Validators.required]),
    preco: new FormControl<number | null>(null, [Validators.required, Validators.min(0.0001)])
  });

  // A unidade do ingrediente escolhido, para mostrar "por g" ao lado do preco.
  protected ingredienteEscolhido = signal<IngredienteOpcao | undefined>(undefined);
  protected unidadeEscolhida = computed(() => this.ingredienteEscolhido()?.unidadePadrao.toLowerCase() ?? '');

  constructor() {
    this.carregar();
  }

  escolherIngrediente(id: number | null): void {
    this.ingredienteEscolhido.set(this.ingredientes().find((ingrediente) => ingrediente.id === id));
    // Se ja esta no catalogo, traz o preco atual para facilitar a alteracao.
    const existente = this.itens().find((item) => item.ingredienteId === id);
    if (existente) {
      this.form.controls.preco.setValue(existente.preco);
    }
  }

  // "Alterar preco" em uma linha da lista: preenche o formulario la em cima.
  alterarPreco(item: CatalogoItem): void {
    this.form.setValue({ ingredienteId: item.ingredienteId, preco: item.preco });
    this.escolherIngrediente(item.ingredienteId);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  salvar(): void {
    const { ingredienteId, preco } = this.form.getRawValue();
    if (this.form.invalid || ingredienteId === null || preco === null) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.servico.salvarNoCatalogo(this.fornecedorId, { ingredienteId, preco }).subscribe({
      next: (item) => {
        this.enviando.set(false);
        this.aviso.open(`${item.ingredienteNome}: preço salvo no catálogo`, 'OK', { duration: 3000 });
        this.form.reset();
        this.ingredienteEscolhido.set(undefined);
        this.recarregarItens();
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  tentarDeNovo(): void {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    forkJoin({
      fornecedor: this.servico.detalhe(this.fornecedorId),
      itens: this.servico.catalogo(this.fornecedorId),
      ingredientes: this.fichaServico.ingredientes()
    }).subscribe({
      next: ({ fornecedor, itens, ingredientes }) => {
        this.fornecedor.set(fornecedor);
        this.itens.set(itens);
        // So ingredientes ativos podem entrar no catalogo.
        this.ingredientes.set(ingredientes.filter((ingrediente) => ingrediente.status === 'ATIVO'));
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  private recarregarItens(): void {
    this.servico.catalogo(this.fornecedorId).subscribe({
      next: (itens) => this.itens.set(itens)
    });
  }
}
