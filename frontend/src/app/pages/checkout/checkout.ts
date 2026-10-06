import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrlPipe } from '../../core/brl.pipe';
import { CarrinhoService } from '../../core/carrinho.service';

// Tela provisoria do checkout. Por enquanto serve para testar o caminho
// carrinho -> login -> checkout. O envio do pedido entra na proxima etapa.
@Component({
  selector: 'app-checkout',
  imports: [RouterLink, BrlPipe],
  template: `
    <h1>Finalizar pedido</h1>
    <p>Total do carrinho: {{ carrinho.valorTotal() | brl }}</p>
    <p>O endereço de entrega e a confirmação do pedido entram aqui na próxima etapa.</p>
    <p><a routerLink="/carrinho">Voltar ao carrinho</a></p>
  `
})
export class Checkout {
  protected carrinho = inject(CarrinhoService);
}
