package com.notoriusfight.api.controller;

import com.notoriusfight.api.dto.LoginRequest;
import com.notoriusfight.api.dto.TokenResponse;
import com.notoriusfight.application.AutenticacaoService;
import com.notoriusfight.application.TokenAcesso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão de token para uma conta válida")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Autenticar e emitir JWT")
    @SecurityRequirements
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        TokenAcesso token = service.autenticar(request.login(), request.senha());
        return new TokenResponse(
                token.valor(),
                "Bearer",
                token.duracaoSegundos(),
                token.expiraEm());
    }
}
