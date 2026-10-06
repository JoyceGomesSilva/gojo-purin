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
import { PratoCardapio } from '../../core/models';

// Detalhe do prato (RF-002): descricao completa, tempo de preparo e o botao
// de adicionar ao carrinho com quantidade e observacoes (RF-003).
@Component({
  selector: 'app-prato',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, BrlPipe],
  templateUrl: './prato.html',
  styleUrl: './prato.scss'
})
export class PratoDetalhe {
  private rota = inject(ActivatedRoute);
  private cardapio = inject(CardapioService);
  private carrinho = inject(CarrinhoService);
  private aviso = inject(MatSnackBar);

  protected prato = signal<PratoCardapio | null>(null);
  protected carregando = signal(true);
  protected falhou = signal(false);

  protected quantidade = signal(1);
  protected observacoes = new FormControl('', { nonNullable: true, validators: [Validators.maxLength(255)] });

  protected kanji = computed(() => kanjiDaCategoria(this.prato()?.categoriaNome ?? ''));
  protected subtotal = computed(() => (this.prato()?.preco ?? 0) * this.quantidade());

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
    this.carrinho.adicionar(prato, this.quantidade(), this.observacoes.value);
    this.aviso.open(`${this.quantidade()} × ${prato.nome} no carrinho`, 'OK', { duration: 3000 });
    this.quantidade.set(1);
    this.observacoes.reset();
  }
}
