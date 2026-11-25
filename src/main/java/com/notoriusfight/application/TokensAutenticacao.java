package com.notoriusfight.application;

import java.time.Instant;

public record TokensAutenticacao(
        TokenAcesso acesso,
        String refreshToken,
        Instant refreshTokenExpiraEm) {
}
