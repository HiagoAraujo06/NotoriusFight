package com.notoriusfight.api.dto;

import java.math.BigDecimal;

public record ItemVendaResponse(Long id, Long artigoId, String artigoDescricao,
                                BigDecimal quantidade, BigDecimal valorUnitario,
                                BigDecimal subtotal) { }
