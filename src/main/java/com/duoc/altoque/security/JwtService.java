package com.duoc.altoque.security;

import com.duoc.altoque.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String SECRETO_DE_DESARROLLO =
            "cambia-este-secreto-por-uno-largo-y-aleatorio-de-32-caracteres-o-mas";

    private final SecretKey key;
    private final long accessExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret debe tener al menos 32 caracteres. Define la variable de entorno JWT_SECRET.");
        }

        if (SECRETO_DE_DESARROLLO.equals(secret)) {
            log.warn("SEGURIDAD: se está usando el secreto JWT por defecto. "
                    + "En producción define la variable de entorno JWT_SECRET (y usa el perfil 'prod').");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .claim("uid", usuario.getId())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + accessExpirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parsear(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }
}
