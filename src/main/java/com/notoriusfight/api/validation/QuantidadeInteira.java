package com.notoriusfight.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A loja vende unidades: aceita 2 e 2.000, mas rejeita 2.5. */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuantidadeInteiraValidator.class)
public @interface QuantidadeInteira {

    String message() default "Quantidade deve ser inteira";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}