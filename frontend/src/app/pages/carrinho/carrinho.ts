import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { BrlPipe } from '../../core/brl.pipe';
import { CarrinhoService } from '../../core/carrinho.service';
import { ItemCarrinho } from '../../core/models';
import { emCentavos, emReais } from '../../core/opcoes';

// Uma linha do carrinho junto com a posicao dela na lista do service
// (a posicao e o que o service usa para alterar ou remover).
interface Linha {
  item: ItemCarrinho;
  posicao: number;
  subtotal: number;
}

// As linhas de um mesmo combo, agrupadas.
interface GrupoCombo {
  chave: string;
  nome: string;
  linhas: Linha[];
  subtotal: number;
}

// Tela do carrinho (RF-003): quantidade, observacoes, subtotal por item,
// total geral, remover e alterar quantidade.
@Component({
  selector: 'app-carrinho',
  imports: [RouterLink, MatButtonModule, BrlPipe],
  templateUrl: './carrinho.html',
  styleUrl: './carrinho.scss'
})
export class Carrinho {
  protected carrinho = inject(CarrinhoService);
  private router = inject(Router);

  private linhas = computed<Linha[]>(() =>
    this.carrinho.itens().map((item, posicao) => ({
      item,
      posicao,
      subtotal: emReais(emCentavos(item.preco) * item.quantidade)
    }))
  );

  // Pratos pedidos fora de combo.
  protected avulsos = computed(() => this.linhas().filter((linha) => linha.item.comboChave === null));

  // Pratos de combo, agrupados pela chave de cada combo adicionado.
  protected combos = computed<GrupoCombo[]>(() => {
    const grupos: GrupoCombo[] = [];
    for (const linha of this.linhas()) {
      const chave = linha.item.comboChave;
      if (chave === null) {
        continue;
      }
      let grupo = grupos.find((g) => g.chave === chave);
      if (!grupo) {
        grupo = { chave, nome: linha.item.comboNome ?? 'Combo', linhas: [], subtotal: 0 };
        grupos.push(grupo);
      }
      grupo.linhas.push(linha);
      grupo.subtotal = emReais(emCentavos(grupo.subtotal) + emCentavos(linha.subtotal));
    }
    return grupos;
  });

  protected nomesDasOpcoes(item: ItemCarrinho): string {
    return item.opcoes.map((opcao) => opcao.nome).join(', ');
  }

  mudarQuantidade(linha: Linha, diferenca: number): void {
    const nova = Math.min(20, linha.item.quantidade + diferenca);
    // Chegando a zero, o service remove a linha.
    this.carrinho.alterarQuantidade(linha.posicao, nova);
  }

  remover(linha: Linha): void {
    this.carrinho.remover(linha.posicao);
  }

  removerCombo(grupo: GrupoCombo): void {
    this.carrinho.removerCombo(grupo.chave);
  }

  esvaziar(): void {
    this.carrinho.limpar();
  }

  // O checkout exige login: quem nao esta logado e levado para a tela de
  // login pelo authGuard e volta para ca depois de entrar (RF-005).
  finalizar(): void {
    this.router.navigate(['/checkout']);
  }
}
