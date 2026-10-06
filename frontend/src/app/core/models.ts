// Formatos dos dados trocados com o back-end (iguais aos DTOs do Spring).

export type Perfil = 'ADMIN' | 'GERENTE' | 'COZINHEIRO' | 'CLIENTE';

// Resposta do login e do cadastro (AuthResponse no back).
export interface Sessao {
  token: string;
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
  telefone: string;
  endereco: string;
}

// Formato padrao de erro do back (ErroResponse).
export interface ErroApi {
  status: number;
  mensagem: string;
  detalhes: string[];
  dataHora: string;
}

// ---------- Cardapio ----------

export interface Categoria {
  id: number;
  nome: string;
  descricao: string | null;
}

// Prato como vem de /api/cardapio (PratoCardapioResponse no back).
export interface PratoCardapio {
  id: number;
  nome: string;
  descricao: string | null;
  fotoUrl: string | null;
  preco: number;
  tempoPreparoMin: number;
  anime: string | null;
  personagem: string | null;
  categoriaId: number;
  categoriaNome: string;
}

// Formato de todas as listagens paginadas do back (PaginaResponse).
export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
  ultima: boolean;
}

// ---------- Complementos ----------

// Uma resposta possivel dentro de um grupo ("Gema mole", "Ovo extra").
export interface Opcao {
  id: number;
  nome: string;
  precoAdicional: number;
}

// Uma pergunta sobre o prato ("Ponto do ovo"), com as respostas possiveis.
export interface GrupoOpcao {
  id: number;
  nome: string;
  obrigatorio: boolean;
  minEscolhas: number;
  maxEscolhas: number;
  opcoes: Opcao[];
}

// Prato na tela de detalhe: os dados do cardapio mais os complementos.
export interface PratoDetalhe extends PratoCardapio {
  gruposOpcoes: GrupoOpcao[];
}

// O que o cliente marcou: para cada id de grupo, os ids das opcoes escolhidas.
export type Selecao = Record<number, number[]>;

// ---------- Combos ----------

export interface ComboEtapa {
  id: number;
  nome: string;
  obrigatoria: boolean;
  pratos: PratoCardapio[];
}

export interface Combo {
  id: number;
  nome: string;
  descricao: string | null;
  tipo: 'PRONTO' | 'MONTAVEL';
  descontoPercentual: number;
  fotoUrl: string | null;
  // So vem preenchidos nos combos prontos.
  precoCheio: number | null;
  precoComDesconto: number | null;
  etapas: ComboEtapa[];
}

// ---------- Carrinho ----------

// Uma linha do carrinho. "preco" e o valor final de UMA unidade:
// preco do prato (ja com o desconto do combo, se houver) + complementos.
export interface ItemCarrinho {
  pratoId: number;
  nome: string;
  preco: number;
  quantidade: number;
  observacoes: string;
  opcoes: Opcao[];
  // Preenchidos quando a linha faz parte de um combo.
  comboId: number | null;
  comboNome: string | null;
  // Identifica cada combo adicionado, para agrupar as linhas dele no carrinho.
  comboChave: string | null;
}

// ---------- Pedidos ----------

export type StatusPedido =
  | 'RECEBIDO'
  | 'CONFIRMADO'
  | 'EM_PREPARO'
  | 'PRONTO'
  | 'SAIU_ENTREGA'
  | 'FINALIZADO'
  | 'CANCELADO';

// O que o front envia no checkout. Nao vai preco: o back calcula tudo.
export interface PedidoItemRequest {
  pratoId: number;
  quantidade: number;
  observacoes: string;
  opcaoIds: number[];
  comboId: number | null;
  comboChave: string | null;
}

export interface PedidoRequest {
  enderecoEntrega: string;
  observacoes: string;
  itens: PedidoItemRequest[];
}

export interface PedidoItem {
  id: number;
  pratoId: number;
  pratoNome: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
  observacoes: string | null;
  comboNome: string | null;
  comboChave: string | null;
  opcoes: string[];
}

// Um ponto da linha do tempo.
export interface StatusHistorico {
  status: StatusPedido;
  dataHora: string;
}

export interface Pedido {
  id: number;
  status: StatusPedido;
  valorTotal: number;
  enderecoEntrega: string;
  observacoes: string | null;
  motivoCancelamento: string | null;
  pago: boolean;
  criadoEm: string;
  atualizadoEm: string;
  itens: PedidoItem[];
  historico: StatusHistorico[];
}

// Resposta leve de /api/pedidos/{id}/status.
export interface PedidoStatus {
  id: number;
  status: StatusPedido;
  motivoCancelamento: string | null;
  atualizadoEm: string;
  historico: StatusHistorico[];
}
