// Um ideograma por categoria, usado no lugar da foto quando o prato ainda nao tem uma.
// Sao os caracteres que cardapios japoneses usam para cada secao.
const KANJI: Record<string, string> = {
  'Prato principal': '主', // prato principal
  Massa: '麺', // massas
  Bento: '弁', // bento
  Petisco: '肴', // petiscos
  Acompanhamento: '副', // acompanhamento
  Sobremesa: '甘', // doce
  Bebida: '飲', // bebida
  'Bebida especial': '特' // especial
};

export function kanjiDaCategoria(nome: string): string {
  return KANJI[nome] ?? '食'; // "comida", para categorias novas
}
