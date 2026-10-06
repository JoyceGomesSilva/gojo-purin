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
