import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminPedidoService } from '../../../core/admin-pedido.service';
import { AuthService, mensagemDeErro } from '../../../core/auth.service';
import { AdminPedido, StatusPedido } from '../../../core/models';
import { acaoParaStatus, nomeDoStatus, podeAvancarPara, podeCancelar, proximoStatus } from '../../../core/status-pedido';

// Os botoes de acao de um pedido no painel: avancar para o proximo status
// e cancelar (com motivo). Usado na lista e na tela de detalhe.
//
// Uso: <app-acoes-pedido [pedido]="pedido" (atualizado)="trocar($event)" />
@Component({
  selector: 'app-acoes-pedido',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './acoes-pedido.html',
  styleUrl: './acoes-pedido.scss'
})
export class AcoesPedido {
  private servico = inject(AdminPedidoService);
  private auth = inject(AuthService);
  private aviso = inject(MatSnackBar);

  pedido = input.required<AdminPedido>();
  // output: avisa a tela "pai" que o pedido mudou, entregando a versao nova.
  atualizado = output<AdminPedido>();

  protected enviando = signal(false);
  protected cancelando = signal(false);
  protected motivo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(255)]
  });

  // Proximo status, se existir e se o perfil logado puder fazer a mudanca.
  protected destino = computed<StatusPedido | null>(() => {
    const proximo = proximoStatus(this.pedido().status);
    return proximo && podeAvancarPara(this.auth.perfil(), proximo) ? proximo : null;
  });

  protected textoDoBotao = computed(() => {
    const destino = this.destino();
    return destino ? acaoParaStatus(destino) : '';
  });

  protected cancelavel = computed(() => podeCancelar(this.auth.perfil(), this.pedido().status));

  avancar(): void {
    const destino = this.destino();
    if (!destino) {
      return;
    }
    this.enviando.set(true);
    this.servico.mudarStatus(this.pedido().id, destino).subscribe({
      next: (pedido) => {
        this.enviando.set(false);
        this.aviso.open(`Pedido nº ${pedido.id}: ${nomeDoStatus(pedido.status)}`, 'OK', { duration: 3000 });
        this.atualizado.emit(pedido);
      },
      error: (erro: unknown) => {
        // Ex.: 422 quando falta ingrediente para confirmar.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  abrirCancelamento(): void {
    this.motivo.reset();
    this.cancelando.set(true);
  }

  fecharCancelamento(): void {
    this.cancelando.set(false);
  }

  confirmarCancelamento(): void {
    if (this.motivo.invalid) {
      this.motivo.markAsTouched();
      return;
    }
    this.enviando.set(true);
    this.servico.cancelar(this.pedido().id, this.motivo.value.trim()).subscribe({
      next: (pedido) => {
        this.enviando.set(false);
        this.cancelando.set(false);
        this.aviso.open(`Pedido nº ${pedido.id} cancelado`, 'OK', { duration: 3000 });
        this.atualizado.emit(pedido);
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }
}
