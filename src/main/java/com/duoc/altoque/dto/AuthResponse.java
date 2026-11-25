package com.duoc.altoque.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiraEnSegundos,
        UsuarioDto usuario) {
}
