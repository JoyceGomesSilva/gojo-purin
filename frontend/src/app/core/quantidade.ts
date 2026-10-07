const formatador = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 });

// Mostra uma quantidade com a unidade: (12000, 'G') vira "12.000 g".
export function formatarQuantidade(valor: number, unidade: string): string {
  return `${formatador.format(valor)} ${unidade.toLowerCase()}`;
}

const formatadorDeCusto = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4
});

// Mostra o custo de uma unidade com ate 4 casas: (0.018, 'G') vira "R$ 0,018 por g".
export function formatarCustoUnitario(valor: number, unidade: string): string {
  return `${formatadorDeCusto.format(valor)} por ${unidade.toLowerCase()}`;
}
