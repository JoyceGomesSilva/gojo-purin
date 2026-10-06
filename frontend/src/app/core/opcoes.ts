import { GrupoOpcao, Opcao, Selecao } from './models';

// Funcoes de apoio para complementos e precos. Sao "puras": recebem dados,
// devolvem um resultado e nao mexem em mais nada, entao servem a qualquer tela.

// As opcoes que o cliente marcou, na ordem em que aparecem na tela.
export function opcoesEscolhidas(grupos: GrupoOpcao[], selecao: Selecao): Opcao[] {
  return grupos.flatMap((grupo) => grupo.opcoes.filter((opcao) => (selecao[grupo.id] ?? []).includes(opcao.id)));
}

// Nomes dos grupos obrigatorios que ainda estao sem resposta.
export function gruposPendentes(grupos: GrupoOpcao[], selecao: Selecao): string[] {
  return grupos
    .filter((grupo) => (selecao[grupo.id] ?? []).length < grupo.minEscolhas)
    .map((grupo) => grupo.nome);
}

export function somaDasOpcoes(opcoes: Opcao[]): number {
  return emReais(opcoes.reduce((soma, opcao) => soma + emCentavos(opcao.precoAdicional), 0));
}

// Preco de um prato dentro de um combo. A conta e feita em centavos (numeros
// inteiros) para dar exatamente o mesmo resultado que o back-end.
export function aplicarDesconto(preco: number, descontoPercentual: number): number {
  return emReais(Math.round((emCentavos(preco) * (100 - descontoPercentual)) / 100));
}

export function emCentavos(valor: number): number {
  return Math.round(valor * 100);
}

export function emReais(centavos: number): number {
  return centavos / 100;
}
