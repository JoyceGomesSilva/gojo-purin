import { Component, input, model } from '@angular/core';
import { BrlPipe } from '../core/brl.pipe';
import { GrupoOpcao, Selecao } from '../core/models';

// Mostra os grupos de complementos de um prato e guarda o que o cliente marcou.
// E usado na tela do prato e na tela do combo.
//
// Uso: <app-escolha-opcoes [grupos]="prato.gruposOpcoes" [(selecao)]="selecao" />
@Component({
  selector: 'app-escolha-opcoes',
  imports: [BrlPipe],
  templateUrl: './escolha-opcoes.html',
  styleUrl: './escolha-opcoes.scss'
})
export class EscolhaOpcoes {
  // input: dado que a tela "pai" entrega para este componente.
  grupos = input.required<GrupoOpcao[]>();
  // Prefixo para os nomes dos campos nao se repetirem quando ha varios na mesma tela.
  prefixo = input('opcoes');
  // model: valor de mao dupla. O pai entrega e tambem recebe as mudancas.
  selecao = model<Selecao>({});

  protected marcada(grupo: GrupoOpcao, opcaoId: number): boolean {
    return (this.selecao()[grupo.id] ?? []).includes(opcaoId);
  }

  // Escolha unica e obrigatoria usa "radio"; o resto usa "checkbox".
  protected unica(grupo: GrupoOpcao): boolean {
    return grupo.obrigatorio && grupo.maxEscolhas === 1;
  }

  // Ja marcou o maximo: as opcoes ainda livres ficam bloqueadas.
  protected bloqueada(grupo: GrupoOpcao, opcaoId: number): boolean {
    const marcadas = this.selecao()[grupo.id] ?? [];
    return !this.unica(grupo) && grupo.maxEscolhas > 1 && marcadas.length >= grupo.maxEscolhas && !marcadas.includes(opcaoId);
  }

  protected regra(grupo: GrupoOpcao): string {
    if (this.unica(grupo)) {
      return 'Escolha 1';
    }
    if (grupo.obrigatorio) {
      return `Escolha de ${grupo.minEscolhas} a ${grupo.maxEscolhas}`;
    }
    return grupo.maxEscolhas === 1 ? 'Opcional' : `Opcional, até ${grupo.maxEscolhas}`;
  }

  protected alternar(grupo: GrupoOpcao, opcaoId: number): void {
    const marcadas = this.selecao()[grupo.id] ?? [];
    let novas: number[];

    if (grupo.maxEscolhas === 1) {
      // Uma so: marcar troca a escolha; desmarcar so vale em grupo opcional.
      novas = marcadas.includes(opcaoId) && !grupo.obrigatorio ? [] : [opcaoId];
    } else if (marcadas.includes(opcaoId)) {
      novas = marcadas.filter((id) => id !== opcaoId);
    } else if (marcadas.length < grupo.maxEscolhas) {
      novas = [...marcadas, opcaoId];
    } else {
      novas = marcadas;
    }

    // Cria um objeto novo em vez de alterar o antigo: e assim que o Angular
    // percebe que o valor mudou.
    this.selecao.set({ ...this.selecao(), [grupo.id]: novas });
  }
}
