package com.notoriusfight.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class QuantidadeInteiraValidator
        implements ConstraintValidator<QuantidadeInteira, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        return value == null || value.stripTrailingZeros().scale() <= 0;
    }
}