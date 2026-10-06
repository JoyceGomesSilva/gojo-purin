import { Pipe, PipeTransform } from '@angular/core';

const formatador = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

// Mostra um numero como dinheiro: 32.9 vira "R$ 32,90".
// Uso no HTML: {{ prato.preco | brl }}
@Pipe({ name: 'brl' })
export class BrlPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    return formatador.format(valor ?? 0);
  }
}
