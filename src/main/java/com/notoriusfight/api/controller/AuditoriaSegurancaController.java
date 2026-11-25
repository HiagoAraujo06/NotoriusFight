package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.EventoSegurancaResponse;
import com.notoriusfight.application.AuditoriaSegurancaService;
import com.notoriusfight.domain.EventoSeguranca;
import com.notoriusfight.domain.TipoEventoSeguranca;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.notoriusfight.api.dto.PaginaResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auditoria de segurança", description = "Consulta administrativa de eventos de segurança")
@RestController
@RequestMapping("/api/auditoria-seguranca")
public class AuditoriaSegurancaController {

    private final AuditoriaSegurancaService service;

    public AuditoriaSegurancaController(AuditoriaSegurancaService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar eventos de segurança", description = "Exige ADMINISTRADOR")
    @GetMapping
    public PaginaResponse<EventoSegurancaResponse> consultar(
            @RequestParam(required = false) TipoEventoSeguranca tipo,
            Pageable pageable) {
        Pageable semOrdenacaoExterna = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize());
        return PaginaResponse.de(
                service.consultar(tipo, semOrdenacaoExterna),
                this::toResponse);
    }

    private EventoSegurancaResponse toResponse(EventoSeguranca evento) {
        return new EventoSegurancaResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getOcorridoEm(),
                evento.getAtorContaId(),
                evento.getAlvoContaId(),
                evento.getEnderecoOrigem());
    }
}
