import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { AdminEstoqueService } from '../../../core/admin-estoque.service';
import { AdminIngredienteService } from '../../../core/admin-ingrediente.service';
import { mensagemDeErro } from '../../../core/auth.service';
import {
  IngredienteRequest,
  MotivoPerda,
  MovimentacaoRequest,
  SaldoEstoque,
  UnidadeIngrediente
} from '../../../core/models';
import { formatarQuantidade } from '../../../core/quantidade';

// Saldo do estoque (RF-031), alertas de minimo (RF-032), as movimentacoes
// manuais (entrada de ajuste, RF-028, e saida por perda, RF-030) e o atalho
// para cadastrar um ingrediente novo ja com o estoque inicial.
@Component({
  selector: 'app-admin-estoque',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './estoque.html',
  styleUrl: '../admin-crud.scss'
})
export class AdminEstoque {
  private fb = inject(FormBuilder);
  private servico = inject(AdminEstoqueService);
  private ingredienteServico = inject(AdminIngredienteService);
  private aviso = inject(MatSnackBar);

  protected formatarQuantidade = formatarQuantidade;

  // ----- Lista -----
  protected saldos = signal<SaldoEstoque[]>([]);
  protected alertas = signal<SaldoEstoque[]>([]);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // Filtros feitos aqui mesmo no navegador: a lista inteira ja esta carregada.
  protected busca = signal('');
  protected soAbaixoDoMinimo = signal(false);
  protected visiveis = computed(() => {
    const trecho = this.busca().trim().toLowerCase();
    return this.saldos().filter(
      (item) =>
        (!this.soAbaixoDoMinimo() || item.abaixoDoMinimo) &&
        (trecho === '' || item.nome.toLowerCase().includes(trecho) || item.sku.toLowerCase().includes(trecho))
    );
  });

  // ----- Formulario de movimentacao -----
  // Em qual ingrediente estamos mexendo (null = formulario fechado) e se e entrada ou saida.
  protected movimentando = signal<SaldoEstoque | null>(null);
  protected tipo = signal<'ENTRADA' | 'SAIDA'>('ENTRADA');
  protected enviando = signal(false);

  protected form = this.fb.nonNullable.group({
    quantidade: [null as number | null, [Validators.required, Validators.min(0.001)]],
    // So na saida (a obrigatoriedade e ligada em abrirForm).
    motivo: ['' as MotivoPerda | ''],
    // So na entrada.
    lote: ['', [Validators.maxLength(50)]],
    validade: [''],
    custoUnitario: [null as number | null, [Validators.min(0)]]
  });

  // ----- Formulario de ingrediente novo -----
  // Cadastra o ingrediente e, se a pessoa quiser, ja registra o estoque inicial.
  protected criando = signal(false);

  protected formNovo = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(120)]],
    sku: ['', [Validators.required, Validators.maxLength(30)]],
    unidadePadrao: ['G' as UnidadeIngrediente, [Validators.required]],
    estoqueMinimo: [0 as number | null, [Validators.required, Validators.min(0)]],
    custoUnitario: [0 as number | null, [Validators.required, Validators.min(0)]],
    // Opcional: zero ou vazio = cadastrar sem dar entrada.
    quantidadeInicial: [null as number | null, [Validators.min(0)]],
    lote: ['', [Validators.maxLength(50)]],
    validade: ['']
  });

  constructor() {
    this.buscar();
  }

  // ---------- Lista ----------

  digitarBusca(evento: Event): void {
    this.busca.set((evento.target as HTMLInputElement).value);
  }

  marcarSoAbaixo(evento: Event): void {
    this.soAbaixoDoMinimo.set((evento.target as HTMLInputElement).checked);
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  // ---------- Ingrediente novo ----------

  abrirNovo(): void {
    this.movimentando.set(null); // so um formulario aberto por vez
    this.formNovo.reset();
    this.criando.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  fecharNovo(): void {
    this.criando.set(false);
  }

  salvarNovo(): void {
    if (this.formNovo.invalid) {
      this.formNovo.markAllAsTouched();
      return;
    }

    const valores = this.formNovo.getRawValue();
    const dados: IngredienteRequest = {
      nome: valores.nome.trim(),
      sku: valores.sku.trim().toUpperCase(),
      unidadePadrao: valores.unidadePadrao,
      estoqueMinimo: valores.estoqueMinimo ?? 0,
      custoUnitario: valores.custoUnitario ?? 0,
      status: 'ATIVO'
    };
    const quantidade = valores.quantidadeInicial ?? 0;

    this.enviando.set(true);
    // Duas chamadas em sequencia: primeiro cria o ingrediente; depois, se foi
    // informado estoque inicial, registra a entrada dele. switchMap faz a
    // segunda chamada usar o resultado da primeira (o id do ingrediente novo).
    this.ingredienteServico
      .criar(dados)
      .pipe(
        switchMap((ingrediente) => {
          if (quantidade <= 0) {
            return of(ingrediente);
          }
          return this.servico
            .movimentar({
              ingredienteId: ingrediente.id,
              tipo: 'ENTRADA',
              quantidade,
              motivo: null,
              lote: valores.lote.trim() || null,
              validade: valores.validade || null,
              custoUnitario: dados.custoUnitario
            })
            .pipe(map(() => ingrediente));
        })
      )
      .subscribe({
        next: (ingrediente) => {
          this.enviando.set(false);
          this.criando.set(false);
          this.aviso.open(`${ingrediente.nome} foi adicionado ao estoque`, 'OK', { duration: 4000 });
          this.buscar();
        },
        error: (erro: unknown) => {
          // Ex.: 409 quando o SKU ja existe.
          this.enviando.set(false);
          this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
          // Se o ingrediente foi criado e so a entrada falhou, ele ja aparece na lista.
          this.buscar();
        }
      });
  }

  // ---------- Movimentacao ----------

  abrirEntrada(item: SaldoEstoque): void {
    this.abrirForm(item, 'ENTRADA');
  }

  abrirSaida(item: SaldoEstoque): void {
    this.abrirForm(item, 'SAIDA');
  }

  fecharForm(): void {
    this.movimentando.set(null);
  }

  salvar(): void {
    const item = this.movimentando();
    if (!item) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();
    const entrada = this.tipo() === 'ENTRADA';
    // Cada tipo envia so os campos dele; os outros vao como null.
    const dados: MovimentacaoRequest = {
      ingredienteId: item.ingredienteId,
      tipo: this.tipo(),
      quantidade: valores.quantidade ?? 0,
      motivo: entrada || valores.motivo === '' ? null : valores.motivo,
      lote: entrada ? valores.lote.trim() || null : null,
      validade: entrada ? valores.validade || null : null,
      custoUnitario: entrada ? valores.custoUnitario : null
    };

    this.enviando.set(true);
    this.servico.movimentar(dados).subscribe({
      next: (saldo) => {
        this.enviando.set(false);
        this.movimentando.set(null);
        this.aviso.open(
          `${saldo.nome}: ${entrada ? 'entrada' : 'saída'} registrada. Saldo: ${formatarQuantidade(saldo.saldo, saldo.unidade)}`,
          'OK',
          { duration: 5000 }
        );
        // Recarrega saldos e alertas (o selo do menu acompanha).
        this.buscar();
      },
      error: (erro: unknown) => {
        // Ex.: 422 quando a saida e maior que o saldo.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  private abrirForm(item: SaldoEstoque, tipo: 'ENTRADA' | 'SAIDA'): void {
    this.criando.set(false); // so um formulario aberto por vez
    this.movimentando.set(item);
    this.tipo.set(tipo);
    // Na entrada, o custo ja vem preenchido com o custo atual do ingrediente.
    this.form.reset({
      quantidade: null,
      motivo: '',
      lote: '',
      validade: '',
      custoUnitario: tipo === 'ENTRADA' ? item.custoUnitario : null
    });
    // O motivo so e obrigatorio na saida.
    this.form.controls.motivo.setValidators(tipo === 'SAIDA' ? [Validators.required] : []);
    this.form.controls.motivo.updateValueAndValidity();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // Busca saldos e alertas ao mesmo tempo (forkJoin espera os dois).
  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    forkJoin({ saldos: this.servico.saldos(), alertas: this.servico.alertas() }).subscribe({
      next: ({ saldos, alertas }) => {
        this.saldos.set(saldos);
        this.alertas.set(alertas);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
