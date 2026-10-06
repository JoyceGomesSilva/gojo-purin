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

// ---------- Carrinho ----------

export interface ItemCarrinho {
  pratoId: number;
  nome: string;
  preco: number;
  quantidade: number;
  observacoes: string;
}
