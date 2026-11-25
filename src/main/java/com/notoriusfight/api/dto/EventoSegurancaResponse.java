package com.notoriusfight.api.dto;

import com.notoriusfight.domain.TipoEventoSeguranca;

import java.time.Instant;

public record EventoSegurancaResponse(
        Long id,
        TipoEventoSeguranca tipo,
        Instant ocorridoEm,
        Long atorContaId,
        Long alvoContaId,
        String enderecoOrigem) {
}
