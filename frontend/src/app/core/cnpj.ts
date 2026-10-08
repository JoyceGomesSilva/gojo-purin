import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// Tira pontos, barra e traco: "48.152.736/0001-01" vira "48152736000101".
export function normalizarCnpj(valor: string): string {
  return valor.replace(/[^0-9A-Za-z]/g, '').toUpperCase();
}

// Coloca a pontuacao: "48152736000101" vira "48.152.736/0001-01".
export function formatarCnpj(valor: string): string {
  const cnpj = normalizarCnpj(valor);
  if (cnpj.length !== 14) {
    return valor;
  }
  return `${cnpj.slice(0, 2)}.${cnpj.slice(2, 5)}.${cnpj.slice(5, 8)}/${cnpj.slice(8, 12)}-${cnpj.slice(12)}`;
}

// A mesma conta do CnpjValidator do back (RN07). Validar aqui so evita uma
// ida ao servidor; quem garante e o back.
export function cnpjValido(valor: string): boolean {
  const cnpj = normalizarCnpj(valor);
  if (!/^[0-9A-Z]{12}[0-9]{2}$/.test(cnpj) || new Set(cnpj).size === 1) {
    return false;
  }
  const digito = (base: string, pesos: number[]) => {
    // Valor de cada caractere = codigo ASCII menos 48 (vale para numeros e letras).
    const soma = pesos.reduce((total, peso, i) => total + (base.charCodeAt(i) - 48) * peso, 0);
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  const primeiro = digito(cnpj.slice(0, 12), [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  const segundo = digito(cnpj.slice(0, 12) + primeiro, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]);
  return cnpj.endsWith(`${primeiro}${segundo}`);
}

// Validador para Reactive Forms. Uso: cnpj: ['', [Validators.required, validadorDeCnpj]]
export const validadorDeCnpj: ValidatorFn = (controle: AbstractControl): ValidationErrors | null => {
  const valor = (controle.value ?? '') as string;
  if (valor.trim() === '') {
    return null; // vazio e problema do Validators.required
  }
  return cnpjValido(valor) ? null : { cnpj: true };
};
