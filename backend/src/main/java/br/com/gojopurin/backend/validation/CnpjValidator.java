package br.com.gojopurin.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Confere um CNPJ pelos dois digitos verificadores (RN07).
//
// Como funciona: os 12 primeiros caracteres sao a "base". Cada um e
// multiplicado por um peso, os resultados sao somados e o resto da divisao
// por 11 da o 13o digito. Repete com 13 caracteres para achar o 14o.
//
// Ja aceita o CNPJ alfanumerico (letras na base) que a Receita Federal
// passou a emitir em 2026: o valor de cada caractere e o codigo dele na
// tabela ASCII menos 48. Para os numeros isso da o proprio numero
// ('7' vale 7); para as letras, 'A' vale 17, 'B' vale 18...
public class CnpjValidator implements ConstraintValidator<Cnpj, String> {

    private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        // Vazio e problema do @NotBlank, nao deste validador.
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return valido(valor);
    }

    // Tira pontos, barra e traco: "48.152.736/0001-01" vira "48152736000101".
    public static String normalizar(String valor) {
        return valor.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
    }

    public static boolean valido(String valor) {
        String cnpj = normalizar(valor);

        // 12 letras ou numeros + 2 digitos verificadores numericos.
        if (!cnpj.matches("[0-9A-Z]{12}[0-9]{2}")) {
            return false;
        }
        // "00000000000000", "11111111111111"... passam na conta, mas nao existem.
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int primeiro = digito(cnpj.substring(0, 12), PESOS_PRIMEIRO);
        int segundo = digito(cnpj.substring(0, 12) + primeiro, PESOS_SEGUNDO);
        return cnpj.endsWith("" + primeiro + segundo);
    }

    private static int digito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (base.charAt(i) - 48) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
