import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EMPTY, catchError, debounceTime, forkJoin, switchMap, tap } from 'rxjs';
import { AdminFichaService } from '../../../core/admin-ficha.service';
import { AdminPratoService } from '../../../core/admin-prato.service';
import { mensagemDeErro } from '../../../core/auth.service';
import { BrlPipe } from '../../../core/brl.pipe';
import {
  FaixaFoodCost,
  FichaTecnica,
  FichaTecnicaItemRequest,
  FichaTecnicaRequest,
  IngredienteOpcao
} from '../../../core/models';

// Cria os campos de UMA linha da ficha (um ingrediente).
// Sem argumento, a linha nasce em branco, com fator de correcao 1.
function criarLinha(item?: FichaTecnicaItemRequest) {
  return new FormGroup({
    ingredienteId: new FormControl<number | null>(item?.ingredienteId ?? null, [Validators.required]),
    quantidade: new FormControl<number | null>(item?.quantidade ?? null, [Validators.required, Validators.min(0.001)]),
    // RN08: fator de correcao nunca menor que 1.
    fatorCorrecao: new FormControl<number | null>(item?.fatorCorrecao ?? 1, [
      Validators.required,
      Validators.min(1),
      Validators.max(9.99)
    ])
  });
}

// "O tipo de uma linha e o tipo que a funcao criarLinha devolve."
type LinhaForm = ReturnType<typeof criarLinha>;

const NOMES_DAS_FAIXAS: Record<FaixaFoodCost, string> = {
  VERDE: 'Saudável (até 30%)',
  AMARELO: 'Atenção (de 30% a 35%)',
  VERMELHO: 'Alto (acima de 35%)'
};

// Mostra o custo unitario com ate 4 casas: "R$ 0,018".
const formatadorDeCusto = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  minimumFractionDigits: 2,
  maximumFractionDigits: 4
});

// Ficha tecnica de um prato (RF-011 a RF-014): ingredientes, quantidades,
// fator de correcao, rendimento e modo de preparo. O custo e o food cost aparecem enquanto a
// pessoa edita, mas quem faz a conta e o back: a tela so mostra o resultado.
@Component({
  selector: 'app-admin-ficha',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, BrlPipe],
  templateUrl: './ficha.html',
  styleUrl: './ficha.scss'
})
export class AdminFicha {
  private servico = inject(AdminFichaService);
  private pratoServico = inject(AdminPratoService);
  private aviso = inject(MatSnackBar);

  // O id do prato vem do endereco: /admin/pratos/12/ficha
  private pratoId = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected ingredientes = signal<IngredienteOpcao[]>([]);
  // O ultimo resultado das contas que o back devolveu.
  protected calculo = signal<FichaTecnica | null>(null);

  protected carregando = signal(true);
  protected falhou = signal(false);
  protected enviando = signal(false);
  // true = ha mudancas na tela que ainda nao foram salvas.
  protected pendente = signal(false);

  // ----- Formulario -----
  protected rendimento = new FormControl<number | null>(1, [
    Validators.required,
    Validators.min(1),
    Validators.max(1000)
  ]);
  // RF-014: o passo a passo para a cozinha. Obrigatorio.
  protected modoPreparo = new FormControl('', {
    nonNullable: true,
    validators: [Validators.required, Validators.maxLength(5000)]
  });
  // FormArray: uma lista de linhas que cresce e diminui.
  protected linhas = new FormArray<LinhaForm>([]);
  protected form = new FormGroup({
    rendimento: this.rendimento,
    modoPreparo: this.modoPreparo,
    linhas: this.linhas
  });

  constructor() {
    this.carregar();

    // "Tempo real" (RF-012): a cada mudanca no formulario, espera a pessoa
    // parar de digitar por 0,4 segundo e pede as contas ao back.
    // switchMap descarta a resposta antiga se uma chamada mais nova ja saiu.
    this.form.valueChanges
      .pipe(
        tap(() => this.pendente.set(true)),
        debounceTime(400),
        switchMap(() => {
          const dados = this.dadosParaSimular();
          if (!dados) {
            return EMPTY;
          }
          // Se a simulacao falhar, a tela fica com a ultima conta que deu certo.
          return this.servico.simular(this.pratoId, dados).pipe(catchError(() => EMPTY));
        }),
        takeUntilDestroyed()
      )
      .subscribe((resultado) => this.calculo.set(resultado));
  }

  // ---------- O que o HTML consulta ----------

  protected nomeDaFaixa = (faixa: FaixaFoodCost) => NOMES_DAS_FAIXAS[faixa];

  protected porcento = (valor: number) =>
    valor.toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) + '%';

  // O ingrediente escolhido em uma linha (ou undefined se ainda nao escolheu).
  protected ingredienteDe(linha: LinhaForm): IngredienteOpcao | undefined {
    const id = linha.controls.ingredienteId.value;
    return this.ingredientes().find((ingrediente) => ingrediente.id === id);
  }

  // Unidade em minusculas para mostrar ao lado da quantidade: "g", "ml", "un".
  protected unidadeDe(linha: LinhaForm): string {
    return this.ingredienteDe(linha)?.unidadePadrao.toLowerCase() ?? '';
  }

  protected custoUnitarioDe(linha: LinhaForm): string {
    const ingrediente = this.ingredienteDe(linha);
    if (!ingrediente) {
      return '';
    }
    return `${formatadorDeCusto.format(ingrediente.custoUnitario)} por ${ingrediente.unidadePadrao.toLowerCase()}`;
  }

  // O custo da linha na ultima conta do back (null se ainda nao foi calculado).
  protected custoDe(linha: LinhaForm): number | null {
    const id = linha.controls.ingredienteId.value;
    const item = this.calculo()?.itens.find((calculado) => calculado.ingredienteId === id);
    return item ? item.custo : null;
  }

  // ---------- Acoes ----------

  adicionarLinha(): void {
    this.linhas.push(criarLinha());
  }

  removerLinha(indice: number): void {
    this.linhas.removeAt(indice);
  }

  tentarDeNovo(): void {
    this.carregar();
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.aviso.open('Preencha o modo de preparo e complete (ou remova) as linhas em branco', 'Fechar', {
        duration: 6000
      });
      return;
    }

    const dados: FichaTecnicaRequest = {
      rendimento: this.rendimento.value ?? 1,
      modoPreparo: this.modoPreparo.value.trim(),
      itens: this.itensCompletos()
    };

    // Um ingrediente so pode aparecer uma vez. Set guarda valores sem repeticao:
    // se ele ficou menor que a lista, havia repetido.
    const ids = dados.itens.map((item) => item.ingredienteId);
    if (new Set(ids).size !== ids.length) {
      this.aviso.open('Há um ingrediente repetido. Junte as quantidades em uma linha só.', 'Fechar', {
        duration: 6000
      });
      return;
    }

    this.enviando.set(true);
    this.servico.salvar(this.pratoId, dados).subscribe({
      next: (ficha) => {
        this.enviando.set(false);
        this.pendente.set(false);
        this.calculo.set(ficha);
        this.aviso.open(`Ficha técnica de ${ficha.pratoNome} salva`, 'OK', { duration: 3000 });
      },
      error: (erro: unknown) => {
        // Ex.: 422 ao esvaziar a ficha de um prato que esta ativo.
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  // Coloca o prato no cardapio sem sair desta tela.
  ativarPrato(): void {
    this.enviando.set(true);
    this.pratoServico.mudarStatus(this.pratoId, 'ATIVO').subscribe({
      next: (prato) => {
        this.enviando.set(false);
        // Atualiza so o status no que esta na tela.
        this.calculo.update((ficha) => (ficha ? { ...ficha, pratoStatus: prato.status } : ficha));
        this.aviso.open(`${prato.nome} está ativo e já aparece no cardápio`, 'OK', { duration: 4000 });
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.aviso.open(mensagemDeErro(erro), 'Fechar', { duration: 8000 });
      }
    });
  }

  // ---------- Internos ----------

  // Busca os ingredientes e a ficha ao mesmo tempo (forkJoin espera os dois).
  private carregar(): void {
    this.carregando.set(true);
    this.falhou.set(false);

    forkJoin({
      ingredientes: this.servico.ingredientes(),
      ficha: this.servico.buscar(this.pratoId)
    }).subscribe({
      next: ({ ingredientes, ficha }) => {
        this.ingredientes.set(ingredientes);
        this.calculo.set(ficha);

        // Monta o formulario com o que esta salvo. emitEvent: false evita que
        // este preenchimento conte como "mudanca feita pela pessoa".
        this.rendimento.setValue(ficha.rendimento, { emitEvent: false });
        this.modoPreparo.setValue(ficha.modoPreparo ?? '', { emitEvent: false });
        this.linhas.clear({ emitEvent: false });
        for (const item of ficha.itens) {
          this.linhas.push(criarLinha(item), { emitEvent: false });
        }

        this.pendente.set(false);
        this.carregando.set(false);
      },
      error: () => {
        this.falhou.set(true);
        this.carregando.set(false);
      }
    });
  }

  // As linhas que ja estao preenchidas por inteiro e com valores validos.
  private itensCompletos(): FichaTecnicaItemRequest[] {
    const itens: FichaTecnicaItemRequest[] = [];
    for (const linha of this.linhas.controls) {
      const { ingredienteId, quantidade, fatorCorrecao } = linha.getRawValue();
      if (linha.valid && ingredienteId !== null && quantidade !== null && fatorCorrecao !== null) {
        itens.push({ ingredienteId, quantidade, fatorCorrecao });
      }
    }
    return itens;
  }

  // Dados para a simulacao: so as linhas completas, sem ingrediente repetido.
  // Devolve null quando nao da para simular (rendimento invalido).
  private dadosParaSimular(): FichaTecnicaRequest | null {
    if (this.rendimento.invalid || this.rendimento.value === null) {
      return null;
    }
    const vistos = new Set<number>();
    const itens = this.itensCompletos().filter((item) => {
      if (vistos.has(item.ingredienteId)) {
        return false;
      }
      vistos.add(item.ingredienteId);
      return true;
    });
    return { rendimento: this.rendimento.value, modoPreparo: this.modoPreparo.value, itens };
  }
}
