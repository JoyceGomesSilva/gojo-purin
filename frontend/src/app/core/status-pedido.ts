import { Perfil, StatusPedido } from './models';

// Os cinco passos que aparecem na linha do tempo do cliente (RF-007), em ordem.
export const PASSOS_DO_PEDIDO: StatusPedido[] = ['RECEBIDO', 'CONFIRMADO', 'EM_PREPARO', 'PRONTO', 'SAIU_ENTREGA'];

const NOMES: Record<StatusPedido, string> = {
  RECEBIDO: 'Recebido',
  CONFIRMADO: 'Confirmado',
  EM_PREPARO: 'Em preparo',
  PRONTO: 'Pronto',
  SAIU_ENTREGA: 'Saiu para entrega',
  FINALIZADO: 'Entregue',
  CANCELADO: 'Cancelado'
};

const EXPLICACOES: Record<StatusPedido, string> = {
  RECEBIDO: 'A cozinha recebeu seu pedido e vai confirmar em instantes.',
  CONFIRMADO: 'Pedido confirmado. Já vamos começar a preparar.',
  EM_PREPARO: 'Seu pedido está sendo preparado.',
  PRONTO: 'Tudo pronto. Estamos aguardando o entregador.',
  SAIU_ENTREGA: 'Seu pedido está a caminho.',
  FINALIZADO: 'Pedido entregue. Bom apetite!',
  CANCELADO: 'Este pedido foi cancelado.'
};

export function nomeDoStatus(status: StatusPedido): string {
  return NOMES[status] ?? status;
}

export function explicacaoDoStatus(status: StatusPedido): string {
  return EXPLICACOES[status] ?? '';
}

// Pedido entregue ou cancelado nao muda mais.
export function pedidoEncerrado(status: StatusPedido): boolean {
  return status === 'FINALIZADO' || status === 'CANCELADO';
}

// ---------- Regras do painel da cozinha ----------
// As mesmas regras existem no back (AdminPedidoService), que e quem decide de
// verdade. Aqui elas servem para mostrar ou esconder os botoes certos.

// O ciclo completo de um pedido (RF-016).
export const CICLO_DO_PEDIDO: StatusPedido[] = [...PASSOS_DO_PEDIDO, 'FINALIZADO'];

// Para onde o pedido vai a seguir. null = nao ha proximo passo.
export function proximoStatus(status: StatusPedido): StatusPedido | null {
  const posicao = CICLO_DO_PEDIDO.indexOf(status);
  if (posicao < 0 || posicao === CICLO_DO_PEDIDO.length - 1) {
    return null;
  }
  return CICLO_DO_PEDIDO[posicao + 1];
}

const ACOES: Partial<Record<StatusPedido, string>> = {
  CONFIRMADO: 'Confirmar pedido',
  EM_PREPARO: 'Iniciar preparo',
  PRONTO: 'Marcar como pronto',
  SAIU_ENTREGA: 'Saiu para entrega',
  FINALIZADO: 'Marcar como entregue'
};

// Texto do botao que leva o pedido ate o status informado.
export function acaoParaStatus(destino: StatusPedido): string {
  return ACOES[destino] ?? nomeDoStatus(destino);
}

// O cozinheiro so inicia o preparo e marca como pronto. Gerente e admin fazem tudo.
export function podeAvancarPara(perfil: Perfil | null, destino: StatusPedido): boolean {
  if (perfil === 'ADMIN' || perfil === 'GERENTE') {
    return true;
  }
  return perfil === 'COZINHEIRO' && (destino === 'EM_PREPARO' || destino === 'PRONTO');
}

// RN04: antes de EM_PREPARO qualquer um da equipe cancela; depois, so gerente ou admin.
export function podeCancelar(perfil: Perfil | null, status: StatusPedido): boolean {
  if (pedidoEncerrado(status)) {
    return false;
  }
  if (perfil === 'ADMIN' || perfil === 'GERENTE') {
    return true;
  }
  return perfil === 'COZINHEIRO' && (status === 'RECEBIDO' || status === 'CONFIRMADO');
}
