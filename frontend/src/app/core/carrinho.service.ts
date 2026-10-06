import { Injectable, computed, signal } from '@angular/core';
import { ItemCarrinho, Opcao, PratoCardapio } from './models';
import { aplicarDesconto, emCentavos, emReais, somaDasOpcoes } from './opcoes';

const CHAVE = 'gojo_purin_carrinho_v2';

// Dados do combo de que uma linha faz parte.
export interface ComboDoItem {
  id: number;
  nome: string;
  descontoPercentual: number;
  chave: string;
}

// O carrinho vive so no navegador (localStorage), como pede o fluxo 8.1 do SRS.
// Ele so vira pedido de verdade no checkout, quando for enviado ao back,
// que confere todos os precos de novo.
@Injectable({ providedIn: 'root' })
export class CarrinhoService {
  private lista = signal<ItemCarrinho[]>(lerCarrinhoSalvo());

  readonly itens = this.lista.asReadonly();
  readonly quantidadeTotal = computed(() => this.lista().reduce((soma, item) => soma + item.quantidade, 0));
  readonly valorTotal = computed(() =>
    emReais(this.lista().reduce((soma, item) => soma + emCentavos(item.preco) * item.quantidade, 0))
  );

  // Adiciona um prato avulso ou um prato de combo.
  // Fora de combo, o mesmo prato com os mesmos complementos e a mesma
  // observacao soma na linha que ja existe.
  adicionar(
    prato: PratoCardapio,
    quantidade: number,
    observacoes: string,
    opcoes: Opcao[] = [],
    combo: ComboDoItem | null = null
  ): void {
    const obs = observacoes.trim();
    const base = combo ? aplicarDesconto(prato.preco, combo.descontoPercentual) : prato.preco;
    const preco = emReais(emCentavos(base) + emCentavos(somaDasOpcoes(opcoes)));

    const novo: ItemCarrinho = {
      pratoId: prato.id,
      nome: prato.nome,
      preco,
      quantidade,
      observacoes: obs,
      opcoes,
      comboId: combo?.id ?? null,
      comboNome: combo?.nome ?? null,
      comboChave: combo?.chave ?? null
    };

    const atual = this.lista();
    const posicao = combo ? -1 : atual.findIndex((item) => mesmaLinha(item, novo));

    if (posicao >= 0) {
      this.salvar(
        atual.map((item, i) => (i === posicao ? { ...item, quantidade: item.quantidade + quantidade } : item))
      );
    } else {
      this.salvar([...atual, novo]);
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

  // Remove todas as linhas de um combo de uma vez (o desconto so vale com o combo inteiro).
  removerCombo(comboChave: string): void {
    this.salvar(this.lista().filter((item) => item.comboChave !== comboChave));
  }

  limpar(): void {
    this.salvar([]);
  }

  private salvar(itens: ItemCarrinho[]): void {
    this.lista.set(itens);
    localStorage.setItem(CHAVE, JSON.stringify(itens));
  }
}

function mesmaLinha(a: ItemCarrinho, b: ItemCarrinho): boolean {
  const ids = (item: ItemCarrinho) => item.opcoes.map((opcao) => opcao.id).sort().join(',');
  return a.comboChave === null && a.pratoId === b.pratoId && a.observacoes === b.observacoes && ids(a) === ids(b);
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
