import { Component, computed, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrlPipe } from '../../core/brl.pipe';
import { CardapioService } from '../../core/cardapio.service';
import { CarrinhoService } from '../../core/carrinho.service';
import { kanjiDaCategoria } from '../../core/categoria-kanji';
import { PratoDetalhe as Prato, Selecao } from '../../core/models';
import { emCentavos, emReais, gruposPendentes, opcoesEscolhidas, somaDasOpcoes } from '../../core/opcoes';
import { EscolhaOpcoes } from '../../shared/escolha-opcoes';

// Detalhe do prato (RF-002): descricao completa, tempo de preparo, complementos
// e o botao de adicionar ao carrinho com quantidade e observacoes (RF-003).
@Component({
  selector: 'app-prato',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    BrlPipe,
    EscolhaOpcoes
  ],
  templateUrl: './prato.html',
  styleUrl: './prato.scss'
})
export class PratoDetalhe {
  private rota = inject(ActivatedRoute);
  private cardapio = inject(CardapioService);
  private carrinho = inject(CarrinhoService);
  private aviso = inject(MatSnackBar);

  protected prato = signal<Prato | null>(null);
  protected carregando = signal(true);
  protected falhou = signal(false);

  protected quantidade = signal(1);
  protected selecao = signal<Selecao>({});
  protected observacoes = new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255)] });

  protected kanji = computed(() => kanjiDaCategoria(this.prato()?.categoriaNome ?? ''));

  // Grupos obrigatorios ainda sem resposta. Enquanto houver algum, nao da para adicionar.
  protected pendentes = computed(() => gruposPendentes(this.prato()?.gruposOpcoes ?? [], this.selecao()));

  // (preco do prato + complementos) x quantidade, com a conta feita em centavos.
  protected subtotal = computed(() => {
    const prato = this.prato();
    if (!prato) {
      return 0;
    }
    const opcoes = opcoesEscolhidas(prato.gruposOpcoes, this.selecao());
    return emReais((emCentavos(prato.preco) + emCentavos(somaDasOpcoes(opcoes))) * this.quantidade());
  });

  constructor() {
    // O ":id" do endereco /prato/7 chega aqui como texto.
    const id = Number(this.rota.snapshot.paramMap.get('id'));

    this.cardapio.prato(id).subscribe({
      next: (prato) => {
        this.prato.set(prato);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  mudarQuantidade(diferenca: number): void {
    this.quantidade.update((atual) => Math.min(20, Math.max(1, atual + diferenca)));
  }

  adicionar(): void {
    const prato = this.prato();
    if (!prato || this.observacoes.invalid) {
      return;
    }
    if (this.pendentes().length > 0) {
      this.aviso.open(`Falta escolher: ${this.pendentes().join(', ')}`, 'OK', { duration: 4000 });
      return;
    }

    const opcoes = opcoesEscolhidas(prato.gruposOpcoes, this.selecao());
    this.carrinho.adicionar(prato, this.quantidade(), this.observacoes.value, opcoes);
    this.aviso.open(`${this.quantidade()} × ${prato.nome} no carrinho`, 'OK', { duration: 3000 });

    this.quantidade.set(1);
    this.selecao.set({});
    this.observacoes.reset();
  }
}
