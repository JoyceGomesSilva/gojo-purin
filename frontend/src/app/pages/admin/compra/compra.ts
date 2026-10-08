import { Component, inject, signal } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import {
  AdminCompraService,
  classeDoStatusDeCompra,
  nomeDoStatusDeCompra
} from '../../../core/admin-compra.service';
import { AdminFornecedorService } from '../../../core/admin-fornecedor.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { BrlPipe } from '../../../core/brl.pipe';
import { DataHoraPipe } from '../../../core/data-hora.pipe';
import { CatalogoItem, Compra, CompraRequest, Fornecedor } from '../../../core/models';
import { formatarCustoUnitario, formatarQuantidade } from '../../../core/quantidade';

// Campos de UMA linha do pedido: o ingrediente e a quantidade.
function criarLinha(ingredienteId: number | null = null, quantidade: number | null = null) {
  return new FormGroup({
    ingredienteId: new FormControl<number | null>(ingredienteId, [Validators.required]),
    quantidade: new FormControl<number | null>(quantidade, [Validators.required, Validators.min(0.001)])
  });
}
type LinhaForm = ReturnType<typeof criarLinha>;

// Campos de recebimento de UM item: lote e validade (opcionais).
function criarRecebimento() {
  return new FormGroup({
    lote: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(50)] }),
    validade: new FormControl('', { nonNullable: true })
  });
}

// Um pedido de compra (RF-024 e RF-025). A mesma tela serve para:
// - criar (endereco /admin/compras/nova);
// - editar enquanto e RASCUNHO e enviar ao fornecedor;
// - registrar o recebimento quando esta ENVIADO;
// - so consultar quando ja foi RECEBIDO ou CANCELADO.
@Component({
  selector: 'app-admin-compra',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    BrlPipe,
    DataHoraPipe
  ],
  templateUrl: './compra.html',
  styleUrls: ['../admin-crud.scss', './compra.scss']
})
export class AdminCompra {
  private servico = inject(AdminCompraService);
  private fornecedorServico = inject(AdminFornecedorService);
  private aviso = inject(MatSnackBar);
  private router = inject(Router);
  private rota = inject(ActivatedRoute);

  protected nomeDoStatus = nomeDoStatusDeCompra;
  protected classeDoStatus = classeDoStatusDeCompra;
  protected formatarQuantidade = formatarQuantidade;
  protected formatarCustoUnitario = formatarCustoUnitario;

  // null = pedido novo (/admin/compras/nova).
  private compraId: number | null = Number(this.rota.snapshot.paramMap.get('id')) || null;

  protected compra = signal<Compra | null>(null);
  protected fornecedores = signal<Fornecedor[]>([]);
  // O catalogo do fornecedor escolhido: so estes ingredientes podem entrar no pedido.
  protected catalogo = signal<CatalogoItem[]>([]);
  protected carregando = signal(true);
  protected falhou = signal(false);
  protected enviando = signal(false);

  // ----- Formulario do rascunho -----
  protected fornecedorId = new FormControl<number | null>(null, [Validators.required]);
  protected linhas = new FormArray<LinhaForm>([]);
  protected form = new FormGroup({ fornecedorId: this.fornecedorId, linhas: this.linhas });

  // ----- Formulario do recebimento (uma entrada por item do pedido) -----
  protected recebimento = new FormArray<ReturnType<typeof criarRecebimento>>([]);

  constructor() {
    this.fornecedorServico.opcoes().subscribe({
      next: (fornecedores) => this.fornecedores.set(fornecedores)
    });

    if (this.compraId) {
      this.carregar(this.compraId);
    } else {
      this.prepararNovo();
    }
  }

  // ---------- O que o HTML consulta ----------

  // Pode mexer nos itens? (pedido novo ou rascunho)
  editavel(): boolean {
    const compra = this.compra();
    return compra === null || compra.status === 'RASCUNHO';
  }

  // O item do catalogo escolhido em uma linha (para mostrar preco e unidade).
  produtoDe(linha: LinhaForm): CatalogoItem | undefined {
    const id = linha.controls.ingredienteId.value;
    return this.catalogo().find((item) => item.ingredienteId === id);
  }

  // Previa do subtotal: quantidade x preco do catalogo. O valor que vale e o
  // que o back calcula ao salvar.
  previaDe(linha: LinhaForm): number {
    const produto = this.produtoDe(linha);
    const quantidade = linha.controls.quantidade.value ?? 0;
    return produto ? Math.round(quantidade * produto.preco * 100) / 100 : 0;
  }

  previaTotal(): number {
    return this.linhas.controls.reduce((total, linha) => total + this.previaDe(linha), 0);
  }

  // ---------- Acoes do rascunho ----------

  // Trocou o fornecedor: os itens antigos podem nao estar no catalogo novo,
  // entao a lista recomeca com uma linha em branco.
  trocarFornecedor(id: number): void {
    this.linhas.clear();
    this.linhas.push(criarLinha());
    this.carregarCatalogo(id);
  }

  adicionarLinha(): void {
    this.linhas.push(criarLinha());
  }

  removerLinha(indice: number): void {
    this.linhas.removeAt(indice);
  }

  salvar(): void {
    const dados = this.dadosDoFormulario();
    if (!dados) {
      return;
    }
    const chamada = this.compraId ? this.servico.editar(this.compraId, dados) : this.servico.criar(dados);
    this.executar(chamada, (compra) => {
      this.aviso.open(`Compra nº ${compra.id} salva como rascunho`, 'OK', { duration: 3000 });
      if (!this.compraId) {
        // Pedido novo: vai para o endereco dele (/admin/compras/15).
        this.router.navigate(['/admin/compras', compra.id]);
      }
    });
  }

  // Salva o que esta na tela e, em seguida, envia ao fornecedor.
  enviar(): void {
    const dados = this.dadosDoFormulario();
    if (!dados || !this.compraId) {
      return;
    }
    const id = this.compraId;
    this.enviando.set(true);
    this.servico.editar(id, dados).subscribe({
      next: () => {
        this.executar(this.servico.enviar(id), (compra) =>
          this.aviso.open(`Compra nº ${compra.id} enviada ao fornecedor`, 'OK', { duration: 4000 })
        );
      },
      error: (erro: unknown) => this.mostrarErro(erro)
    });
  }

  // ---------- Recebimento ----------

  receber(): void {
    const compra = this.compra();
    if (!compra || this.recebimento.invalid) {
      this.recebimento.markAllAsTouched();
      return;
    }
    const dados = {
      itens: compra.itens.map((item, indice) => {
        const { lote, validade } = this.recebimento.at(indice).getRawValue();
        return { itemId: item.id, lote: lote.trim() || null, validade: validade || null };
      })
    };
    this.executar(this.servico.receber(compra.id, dados), (recebida) =>
      this.aviso.open(
        `Compra nº ${recebida.id} recebida: estoque e custos dos ingredientes atualizados`,
        'OK',
        { duration: 5000 }
      )
    );
  }

  // ---------- Cancelar ----------

  cancelar(): void {
    const compra = this.compra();
    if (!compra) {
      return;
    }
    this.enviando.set(true);
    this.servico.cancelar(compra.id).subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.open(`Compra nº ${compra.id} cancelada`, 'OK', { duration: 4000 });
        this.carregar(compra.id);
      },
      error: (erro: unknown) => this.mostrarErro(erro)
    });
  }

  tentarDeNovo(): void {
    if (this.compraId) {
      this.carregar(this.compraId);
    }
  }

  // ---------- Internos ----------

  // Pedido novo. Se veio da cotacao (?fornecedor=2&ingrediente=5), ja
  // comeca com o fornecedor escolhido e o ingrediente na primeira linha.
  private prepararNovo(): void {
    const parametros = this.rota.snapshot.queryParamMap;
    const fornecedor = Number(parametros.get('fornecedor')) || null;
    const ingrediente = Number(parametros.get('ingrediente')) || null;

    this.linhas.push(criarLinha(ingrediente));
    if (fornecedor) {
      this.fornecedorId.setValue(fornecedor);
      this.carregarCatalogo(fornecedor);
    }
    this.carregando.set(false);
  }

  private carregar(id: number): void {
    this.carregando.set(true);
    this.falhou.set(false);
    this.servico.detalhe(id).subscribe({
      next: (compra) => {
        this.mostrar(compra);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  // Coloca na tela o pedido que veio do back.
  private mostrar(compra: Compra): void {
    this.compra.set(compra);

    this.fornecedorId.setValue(compra.fornecedorId);
    this.linhas.clear();
    for (const item of compra.itens) {
      this.linhas.push(criarLinha(item.ingredienteId, item.quantidade));
    }

    // Um par lote/validade para cada item, na mesma ordem.
    this.recebimento.clear();
    for (let i = 0; i < compra.itens.length; i++) {
      this.recebimento.push(criarRecebimento());
    }

    if (compra.status === 'RASCUNHO') {
      this.carregarCatalogo(compra.fornecedorId);
    }
  }

  private carregarCatalogo(fornecedorId: number): void {
    this.fornecedorServico.catalogo(fornecedorId).subscribe({
      next: (itens) => this.catalogo.set(itens),
      error: () => this.catalogo.set([])
    });
  }

  // Monta o que vai para o back. Devolve null (e avisa) se algo esta errado.
  private dadosDoFormulario(): CompraRequest | null {
    if (this.form.invalid || this.linhas.length === 0) {
      this.form.markAllAsTouched();
      this.aviso.open('Escolha o fornecedor e preencha (ou remova) as linhas em branco', 'Fechar', {
        duration: 6000
      });
      return null;
    }
    const itens = this.linhas.controls.map((linha) => {
      const { ingredienteId, quantidade } = linha.getRawValue();
      return { ingredienteId: ingredienteId ?? 0, quantidade: quantidade ?? 0 };
    });
    const ids = itens.map((item) => item.ingredienteId);
    if (new Set(ids).size !== ids.length) {
      this.aviso.open('Há um ingrediente repetido. Junte as quantidades em uma linha só.', 'Fechar', {
        duration: 6000
      });
      return null;
    }
    return { fornecedorId: this.fornecedorId.value ?? 0, itens };
  }

  // Faz a chamada, mostra o pedido atualizado e roda "depois" se der certo.
  private executar(chamada: Observable<Compra>, depois: (compra: Compra) => void): void {
    this.enviando.set(true);
    chamada.subscribe({
      next: (compra) => {
        this.enviando.set(false);
        this.mostrar(compra);
        depois(compra);
      },
      error: (erro: unknown) => this.mostrarErro(erro)
    });
  }

  private mostrarErro(erro: unknown): void {
    this.enviando.set(false);
    this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
  }
}
