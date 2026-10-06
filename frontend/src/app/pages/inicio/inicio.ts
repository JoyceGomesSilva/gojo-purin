import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { BrlPipe } from '../../core/brl.pipe';
import { CardapioService } from '../../core/cardapio.service';
import { kanjiDaCategoria } from '../../core/categoria-kanji';
import { Categoria, Combo, PratoCardapio } from '../../core/models';

interface Secao {
  nome: string;
  kanji: string;
  pratos: PratoCardapio[];
}

// Tela inicial: o cardapio publico (RF-001). Nao exige login.
@Component({
  selector: 'app-inicio',
  imports: [RouterLink, MatButtonModule, BrlPipe],
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss'
})
export class Inicio {
  private cardapio = inject(CardapioService);

  protected categorias = signal<Categoria[]>([]);
  protected pratos = signal<PratoCardapio[]>([]);
  protected combos = signal<Combo[]>([]);
  protected categoriaId = signal<number | null>(null);

  protected carregando = signal(true);
  protected falhou = signal(false);
  protected temMais = signal(false);
  private pagina = 0;

  // Agrupa os pratos por categoria, na ordem em que o back devolveu.
  // "computed" refaz a conta sozinho sempre que a lista de pratos muda.
  protected secoes = computed<Secao[]>(() => {
    const secoes: Secao[] = [];
    for (const prato of this.pratos()) {
      let secao = secoes.find((s) => s.nome === prato.categoriaNome);
      if (!secao) {
        secao = { nome: prato.categoriaNome, kanji: kanjiDaCategoria(prato.categoriaNome), pratos: [] };
        secoes.push(secao);
      }
      secao.pratos.push(prato);
    }
    return secoes;
  });

  constructor() {
    this.cardapio.categorias().subscribe({
      next: (categorias) => this.categorias.set(categorias),
      error: () => this.categorias.set([])
    });
    // Os combos aparecem no topo do cardapio. Se a busca falhar, a secao so nao aparece.
    this.cardapio.combos().subscribe({
      next: (combos) => this.combos.set(combos),
      error: () => this.combos.set([])
    });
    this.buscar();
  }

  // Nomes dos pratos de um combo pronto, para o resumo do cartao.
  protected pratosDoCombo(combo: Combo): string {
    return combo.etapas.map((etapa) => etapa.pratos[0]?.nome).join(' + ');
  }

  filtrar(categoriaId: number | null): void {
    if (categoriaId === this.categoriaId()) {
      return;
    }
    this.categoriaId.set(categoriaId);
    this.pagina = 0;
    this.pratos.set([]);
    this.buscar();
  }

  verMais(): void {
    this.pagina++;
    this.buscar();
  }

  tentarDeNovo(): void {
    this.buscar();
  }

  private buscar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    this.cardapio.pratos(this.categoriaId(), this.pagina).subscribe({
      next: (resposta) => {
        // Na pagina 0 troca a lista; nas seguintes, acrescenta ao final.
        this.pratos.update((atuais) => (resposta.pagina === 0 ? resposta.conteudo : [...atuais, ...resposta.conteudo]));
        this.temMais.set(!resposta.ultima);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }
}
