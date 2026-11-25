package com.notoriusfight.api.dto;

import com.notoriusfight.domain.StatusVenda;

import java.time.LocalDateTime;

public record VendaResumoResponse(Long id, Long clienteId, String clienteNome,
                                  Long vendedorId, String vendedorNome,
                                  LocalDateTime dataVenda, StatusVenda status) { }
