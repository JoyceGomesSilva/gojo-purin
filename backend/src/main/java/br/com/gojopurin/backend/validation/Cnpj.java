package br.com.gojopurin.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Anotacao propria de validacao (RN07): colocada em um campo do DTO, faz o
// Bean Validation conferir o CNPJ pelo algoritmo dos digitos verificadores.
// Uso: @Cnpj String cnpj
// Quem faz a conta e a classe CnpjValidator (ligada aqui em validatedBy).
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CnpjValidator.class)
public @interface Cnpj {

    String message() default "CNPJ inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
