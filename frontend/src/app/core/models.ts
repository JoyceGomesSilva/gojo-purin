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

// ---------- Painel: pedidos ----------

// Pedido como a equipe ve: os dados do pedido mais os do cliente e o canal.
export interface AdminPedido extends Pedido {
  canal: string;
  clienteNome: string;
  clienteTelefone: string | null;
  clienteEmail: string;
}

// Filtros da lista de pedidos do painel. Texto vazio = sem filtro.
export interface FiltrosDePedido {
  status: string;
  canal: string;
  de: string;
  ate: string;
}

// ---------- Painel: usuarios internos ----------

// Os perfis que o admin cria pelo painel.
export type PerfilInterno = 'GERENTE' | 'COZINHEIRO';

// Usuario interno como vem de /api/admin/usuarios (UsuarioResponse no back).
// Nao existe campo de senha: o back nunca devolve.
export interface UsuarioInterno {
  id: number;
  nome: string;
  email: string;
  perfil: PerfilInterno;
  telefone: string | null;
  status: 'ATIVO' | 'INATIVO';
  criadoEm: string;
}

// O que o front envia para criar ou editar. Na edicao, senha null = manter a atual.
export interface UsuarioInternoRequest {
  nome: string;
  email: string;
  perfil: PerfilInterno;
  telefone: string | null;
  senha: string | null;
}

// Filtros da lista de usuarios. Texto vazio = sem filtro.
export interface FiltrosDeUsuario {
  perfil: string;
  status: string;
}

// ---------- Painel: categorias e pratos ----------

// Categoria como vem de /api/admin/categorias (AdminCategoriaResponse no back).
export interface AdminCategoria {
  id: number;
  nome: string;
  descricao: string | null;
  ordem: number;
  status: 'ATIVO' | 'INATIVO';
}

export interface AdminCategoriaRequest {
  nome: string;
  descricao: string | null;
  ordem: number;
  status: 'ATIVO' | 'INATIVO';
}

export type StatusPrato = 'ATIVO' | 'INATIVO' | 'PAUSADO';

// Prato como vem de /api/admin/pratos (AdminPratoResponse no back).
export interface AdminPrato {
  id: number;
  nome: string;
  descricao: string | null;
  fotoUrl: string | null;
  precoVenda: number;
  tempoPreparoMin: number;
  anime: string | null;
  personagem: string | null;
  categoriaId: number;
  categoriaNome: string;
  status: StatusPrato;
  modoPreparo: string | null;
  // true = tem ficha tecnica com ingredientes, entao pode ficar ATIVO.
  temFicha: boolean;
}

export interface AdminPratoRequest {
  nome: string;
  descricao: string | null;
  fotoUrl: string | null;
  precoVenda: number;
  tempoPreparoMin: number;
  categoriaId: number;
  status: StatusPrato;
  anime: string | null;
  personagem: string | null;
}

// Filtros da lista de pratos do painel. Texto vazio = sem filtro.
export interface FiltrosDePrato {
  categoriaId: string;
  status: string;
  busca: string;
}

// ---------- Painel: ficha tecnica e custos ----------

// Ingrediente resumido, para o campo de escolha da ficha (IngredienteOpcaoResponse no back).
export interface IngredienteOpcao {
  id: number;
  nome: string;
  unidadePadrao: string;
  custoUnitario: number;
  status: string;
}

export type FaixaFoodCost = 'VERDE' | 'AMARELO' | 'VERMELHO';

// Uma linha da ficha, com o custo ja calculado pelo back.
export interface FichaTecnicaItem {
  ingredienteId: number;
  ingredienteNome: string;
  unidade: string;
  quantidade: number;
  fatorCorrecao: number;
  custoUnitario: number;
  custo: number;
}

// A ficha de um prato com todas as contas (FichaTecnicaResponse no back).
export interface FichaTecnica {
  pratoId: number;
  pratoNome: string;
  pratoStatus: StatusPrato;
  precoVenda: number;
  rendimento: number;
  modoPreparo: string | null;
  itens: FichaTecnicaItem[];
  custoTotal: number;
  custoPorcao: number;
  // Os tres abaixo vem null enquanto a ficha nao tem ingredientes.
  foodCost: number | null;
  faixa: FaixaFoodCost | null;
  aviso: string | null;
}

export interface FichaTecnicaItemRequest {
  ingredienteId: number;
  quantidade: number;
  fatorCorrecao: number;
}

// O que o front envia para salvar ou simular. Nao vai custo: o back calcula tudo.
export interface FichaTecnicaRequest {
  rendimento: number;
  modoPreparo: string;
  itens: FichaTecnicaItemRequest[];
}

// ---------- Painel: estoque ----------

export type UnidadeIngrediente = 'G' | 'ML' | 'UN' | 'KG' | 'L';

// Ingrediente completo, como vem de /api/admin/ingredientes (IngredienteResponse no back).
export interface Ingrediente {
  id: number;
  nome: string;
  sku: string;
  unidadePadrao: UnidadeIngrediente;
  estoqueMinimo: number;
  custoUnitario: number;
  status: 'ATIVO' | 'INATIVO';
}

export interface IngredienteRequest {
  nome: string;
  sku: string;
  unidadePadrao: UnidadeIngrediente;
  estoqueMinimo: number;
  custoUnitario: number;
  status: 'ATIVO' | 'INATIVO';
}

// Filtros da lista de ingredientes. Texto vazio = sem filtro.
export interface FiltrosDeIngrediente {
  status: string;
  busca: string;
}

// Saldo atual de um ingrediente (SaldoResponse no back).
export interface SaldoEstoque {
  ingredienteId: number;
  nome: string;
  sku: string;
  unidade: string;
  saldo: number;
  estoqueMinimo: number;
  custoUnitario: number;
  abaixoDoMinimo: boolean;
}

export type TipoMovimentacao = 'ENTRADA' | 'SAIDA' | 'ESTORNO';
export type MotivoPerda = 'DESPERDICIO' | 'VENCIMENTO' | 'QUEBRA' | 'USO_INTERNO';

// O que o front envia para registrar uma entrada ou saida manual.
export interface MovimentacaoRequest {
  ingredienteId: number;
  tipo: 'ENTRADA' | 'SAIDA';
  quantidade: number;
  // So na saida.
  motivo: MotivoPerda | null;
  // Os tres abaixo so na entrada.
  lote: string | null;
  validade: string | null;
  custoUnitario: number | null;
}

// Uma linha do historico de estoque (MovimentacaoResponse no back).
export interface Movimentacao {
  id: number;
  dataHora: string;
  ingredienteId: number;
  ingredienteNome: string;
  unidade: string;
  tipo: TipoMovimentacao;
  motivo: string;
  quantidade: number;
  lote: string | null;
  validade: string | null;
  custoUnitario: number | null;
  usuarioNome: string;
  pedidoId: number | null;
  pedidoCompraId: number | null;
}

// Filtros do historico. Texto vazio = sem filtro.
export interface FiltrosDeMovimentacao {
  ingredienteId: string;
  tipo: string;
}
