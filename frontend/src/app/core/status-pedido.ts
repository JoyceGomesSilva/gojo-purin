import { StatusPedido } from './models';

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
