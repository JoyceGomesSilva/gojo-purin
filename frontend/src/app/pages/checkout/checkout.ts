import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { mensagemDeErro } from '../../core/auth.service';
import { BrlPipe } from '../../core/brl.pipe';
import { CarrinhoService } from '../../core/carrinho.service';
import { ErroApi, ItemCarrinho, PedidoRequest } from '../../core/models';
import { emCentavos, emReais } from '../../core/opcoes';
import { PedidoService } from '../../core/pedido.service';

// Checkout (RF-006): resumo do carrinho, endereco de entrega e o botao
// "Confirmar pedido". O pagamento e simulado.
@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, BrlPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.scss'
})
export class Checkout {
  protected carrinho = inject(CarrinhoService);
  private pedidos = inject(PedidoService);
  private router = inject(Router);

  protected endereco = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(255)]
  });
  protected observacoes = new FormControl('', { nonNullable: true, validators: [Validators.maxLength(500)] });

  protected enviando = signal(false);
  // Mensagem e detalhes quando o back recusa o pedido (ex.: falta de estoque).
  protected erro = signal('');
  protected detalhesDoErro = signal<string[]>([]);

  constructor() {
    // Sem itens nao ha o que finalizar.
    if (this.carrinho.itens().length === 0) {
      this.router.navigate(['/carrinho']);
      return;
    }

    // Traz o endereco do cadastro ja preenchido. O cliente pode trocar.
    this.pedidos.enderecoCadastrado().subscribe({
      next: (resposta) => {
        if (!this.endereco.value && resposta.endereco) {
          this.endereco.setValue(resposta.endereco);
        }
      },
      error: () => {
        // Se nao der para buscar, o campo so fica vazio.
      }
    });
  }

  protected subtotal(item: ItemCarrinho): number {
    return emReais(emCentavos(item.preco) * item.quantidade);
  }

  protected nomesDasOpcoes(item: ItemCarrinho): string {
    return item.opcoes.map((opcao) => opcao.nome).join(', ');
  }

  confirmar(): void {
    if (this.endereco.invalid || this.observacoes.invalid) {
      this.endereco.markAsTouched();
      return;
    }

    // Monta o pedido so com ids e quantidades. Os precos nao vao:
    // o back calcula tudo de novo a partir do banco.
    const pedido: PedidoRequest = {
      enderecoEntrega: this.endereco.value.trim(),
      observacoes: this.observacoes.value.trim(),
      itens: this.carrinho.itens().map((item) => ({
        pratoId: item.pratoId,
        quantidade: item.quantidade,
        observacoes: item.observacoes,
        opcaoIds: item.opcoes.map((opcao) => opcao.id),
        comboId: item.comboId,
        comboChave: item.comboChave
      }))
    };

    this.enviando.set(true);
    this.erro.set('');
    this.detalhesDoErro.set([]);

    this.pedidos.criar(pedido).subscribe({
      next: (criado) => {
        this.carrinho.limpar();
        this.router.navigate(['/pedido', criado.id], { queryParams: { novo: 1 } });
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        const corpo = erro instanceof HttpErrorResponse ? (erro.error as Partial<ErroApi> | null) : null;
        if (corpo?.mensagem) {
          this.erro.set(corpo.mensagem);
          this.detalhesDoErro.set(corpo.detalhes ?? []);
        } else {
          this.erro.set(mensagemDeErro(erro));
        }
      }
    });
  }
}
