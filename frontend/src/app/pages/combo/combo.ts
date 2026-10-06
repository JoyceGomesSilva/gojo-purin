import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BrlPipe } from '../../core/brl.pipe';
import { CardapioService } from '../../core/cardapio.service';
import { CarrinhoService } from '../../core/carrinho.service';
import { Combo, ComboEtapa, Opcao, PratoCardapio, PratoDetalhe, Selecao } from '../../core/models';
import { aplicarDesconto, emCentavos, emReais, gruposPendentes, opcoesEscolhidas, somaDasOpcoes } from '../../core/opcoes';
import { EscolhaOpcoes } from '../../shared/escolha-opcoes';

// Uma linha do resumo: o prato escolhido em uma etapa, com seus precos.
interface LinhaResumo {
  etapa: ComboEtapa;
  prato: PratoCardapio;
  opcoes: Opcao[];
  precoCheio: number;
  precoFinal: number;
}

// Tela do combo. Serve aos dois tipos:
// PRONTO: os pratos ja vem marcados, o cliente so responde os complementos.
// MONTAVEL: o cliente escolhe um prato em cada etapa.
@Component({
  selector: 'app-combo',
  imports: [RouterLink, MatButtonModule, BrlPipe, EscolhaOpcoes],
  templateUrl: './combo.html',
  styleUrl: './combo.scss'
})
export class ComboPagina {
  private rota = inject(ActivatedRoute);
  private router = inject(Router);
  private cardapio = inject(CardapioService);
  private carrinho = inject(CarrinhoService);
  private aviso = inject(MatSnackBar);

  protected combo = signal<Combo | null>(null);
  protected carregando = signal(true);
  protected falhou = signal(false);

  // Para cada etapa (pelo id), o id do prato escolhido. null = "nao quero".
  protected escolhas = signal<Record<number, number | null>>({});
  // Para cada etapa, os complementos marcados no prato escolhido.
  protected selecoes = signal<Record<number, Selecao>>({});
  // Detalhes ja buscados no back (com os complementos), guardados pelo id do prato.
  private detalhes = signal<Record<number, PratoDetalhe>>({});

  // O que ja foi escolhido, com os precos calculados.
  protected linhas = computed<LinhaResumo[]>(() => {
    const combo = this.combo();
    if (!combo) {
      return [];
    }
    const linhas: LinhaResumo[] = [];
    for (const etapa of combo.etapas) {
      const prato = etapa.pratos.find((p) => p.id === this.escolhas()[etapa.id]);
      if (!prato) {
        continue;
      }
      const grupos = this.detalhes()[prato.id]?.gruposOpcoes ?? [];
      const opcoes = opcoesEscolhidas(grupos, this.selecoes()[etapa.id] ?? {});
      const adicionais = emCentavos(somaDasOpcoes(opcoes));
      linhas.push({
        etapa,
        prato,
        opcoes,
        precoCheio: emReais(emCentavos(prato.preco) + adicionais),
        precoFinal: emReais(emCentavos(aplicarDesconto(prato.preco, combo.descontoPercentual)) + adicionais)
      });
    }
    return linhas;
  });

  protected totalCheio = computed(() => emReais(this.linhas().reduce((s, l) => s + emCentavos(l.precoCheio), 0)));
  protected total = computed(() => emReais(this.linhas().reduce((s, l) => s + emCentavos(l.precoFinal), 0)));

  // O que ainda falta para poder adicionar o combo ao carrinho.
  protected pendencias = computed<string[]>(() => {
    const combo = this.combo();
    if (!combo) {
      return [];
    }
    const faltas: string[] = [];
    for (const etapa of combo.etapas) {
      const pratoId = this.escolhas()[etapa.id];
      if (pratoId == null) {
        if (etapa.obrigatoria) {
          faltas.push(etapa.nome);
        }
        continue;
      }
      const detalhe = this.detalhes()[pratoId];
      if (!detalhe) {
        faltas.push(`${etapa.nome} (carregando)`);
        continue;
      }
      for (const grupo of gruposPendentes(detalhe.gruposOpcoes, this.selecoes()[etapa.id] ?? {})) {
        faltas.push(`${grupo} (${detalhe.nome})`);
      }
    }
    return faltas;
  });

  constructor() {
    const id = Number(this.rota.snapshot.paramMap.get('id'));

    this.cardapio.combo(id).subscribe({
      next: (combo) => {
        this.combo.set(combo);
        // Etapa obrigatoria com um prato so ja vem marcada (caso do combo pronto).
        for (const etapa of combo.etapas) {
          if (etapa.obrigatoria && etapa.pratos.length === 1) {
            this.escolher(etapa, etapa.pratos[0].id);
          }
        }
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  protected fixa(etapa: ComboEtapa): boolean {
    return etapa.obrigatoria && etapa.pratos.length === 1;
  }

  protected comDesconto(preco: number): number {
    return aplicarDesconto(preco, this.combo()?.descontoPercentual ?? 0);
  }

  // Detalhe (com complementos) do prato escolhido na etapa, se ja chegou do back.
  protected detalheDaEtapa(etapa: ComboEtapa): PratoDetalhe | null {
    const pratoId = this.escolhas()[etapa.id];
    return pratoId == null ? null : (this.detalhes()[pratoId] ?? null);
  }

  protected escolher(etapa: ComboEtapa, pratoId: number | null): void {
    this.escolhas.update((atual) => ({ ...atual, [etapa.id]: pratoId }));
    // Trocar de prato zera os complementos marcados naquela etapa.
    this.selecoes.update((atual) => ({ ...atual, [etapa.id]: {} }));

    // Busca os complementos do prato na primeira vez que ele e escolhido.
    if (pratoId !== null && !this.detalhes()[pratoId]) {
      this.cardapio.prato(pratoId).subscribe({
        next: (detalhe) => this.detalhes.update((atual) => ({ ...atual, [pratoId]: detalhe })),
        error: () => this.aviso.open('Não deu para carregar os complementos. Escolha o prato de novo.', 'OK', { duration: 5000 })
      });
    }
  }

  protected definirSelecao(etapaId: number, selecao: Selecao): void {
    this.selecoes.update((atual) => ({ ...atual, [etapaId]: selecao }));
  }

  adicionar(): void {
    const combo = this.combo();
    if (!combo) {
      return;
    }
    if (this.pendencias().length > 0) {
      this.aviso.open(`Falta escolher: ${this.pendencias().join(', ')}`, 'OK', { duration: 5000 });
      return;
    }

    // A chave identifica este combo no carrinho, para as linhas dele ficarem juntas.
    const dadosDoCombo = {
      id: combo.id,
      nome: combo.nome,
      descontoPercentual: combo.descontoPercentual,
      chave: `${combo.id}-${Date.now()}`
    };
    for (const linha of this.linhas()) {
      this.carrinho.adicionar(linha.prato, 1, '', linha.opcoes, dadosDoCombo);
    }

    this.aviso.open(`${combo.nome} no carrinho`, 'OK', { duration: 3000 });
    this.router.navigate(['/']);
  }
}
