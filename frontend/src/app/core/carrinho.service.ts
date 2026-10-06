import { Injectable, computed, signal } from '@angular/core';
import { ItemCarrinho, PratoCardapio } from './models';

const CHAVE = 'gojo_purin_carrinho';

// O carrinho vive so no navegador (localStorage), como pede o fluxo 8.1 do SRS.
// Ele so vira pedido de verdade no checkout, quando for enviado ao back.
@Injectable({ providedIn: 'root' })
export class CarrinhoService {
  private lista = signal<ItemCarrinho[]>(lerCarrinhoSalvo());

  readonly itens = this.lista.asReadonly();
  readonly quantidadeTotal = computed(() => this.lista().reduce((soma, item) => soma + item.quantidade, 0));
  readonly valorTotal = computed(() => this.lista().reduce((soma, item) => soma + item.preco * item.quantidade, 0));

  // Mesmo prato com a mesma observacao soma na linha que ja existe.
  adicionar(prato: PratoCardapio, quantidade: number, observacoes: string): void {
    const obs = observacoes.trim();
    const atual = this.lista();
    const posicao = atual.findIndex((item) => item.pratoId === prato.id && item.observacoes === obs);

    if (posicao >= 0) {
      this.salvar(
        atual.map((item, i) => (i === posicao ? { ...item, quantidade: item.quantidade + quantidade } : item))
      );
    } else {
      this.salvar([
        ...atual,
        { pratoId: prato.id, nome: prato.nome, preco: prato.preco, quantidade, observacoes: obs }
      ]);
    }
  }

  alterarQuantidade(posicao: number, quantidade: number): void {
    if (quantidade < 1) {
      this.remover(posicao);
      return;
    }
    this.salvar(this.lista().map((item, i) => (i === posicao ? { ...item, quantidade } : item)));
  }

  remover(posicao: number): void {
    this.salvar(this.lista().filter((_, i) => i !== posicao));
  }

  limpar(): void {
    this.salvar([]);
  }

  private salvar(itens: ItemCarrinho[]): void {
    this.lista.set(itens);
    localStorage.setItem(CHAVE, JSON.stringify(itens));
  }
}

function lerCarrinhoSalvo(): ItemCarrinho[] {
  try {
    const texto = localStorage.getItem(CHAVE);
    const itens = texto ? (JSON.parse(texto) as ItemCarrinho[]) : [];
    return Array.isArray(itens) ? itens : [];
  } catch {
    return [];
  }
}
