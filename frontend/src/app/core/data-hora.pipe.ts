import { Pipe, PipeTransform } from '@angular/core';

const dataEHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
const soHora = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'short' });

// Mostra a data que vem do back ("2026-10-06T16:10:00") em formato brasileiro.
// {{ pedido.criadoEm | dataHora }}        -> 06/10/2026, 16:10
// {{ ponto.dataHora | dataHora: 'hora' }} -> 16:10
@Pipe({ name: 'dataHora' })
export class DataHoraPipe implements PipeTransform {
  transform(valor: string | null | undefined, formato: 'completo' | 'hora' = 'completo'): string {
    if (!valor) {
      return '';
    }
    const data = new Date(valor);
    if (Number.isNaN(data.getTime())) {
      return '';
    }
    return formato === 'hora' ? soHora.format(data) : dataEHora.format(data);
  }
}
