package com.notoriusfight.api.dto;

import com.notoriusfight.api.validation.QuantidadeInteira;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemVendaRequest(
        @NotNull @Schema(example = "1") Long artigoId,
        @NotNull @DecimalMin(value = "1") @Digits(integer = 15, fraction = 3)
        @QuantidadeInteira
        @Schema(example = "2.000") BigDecimal quantidade) { }
