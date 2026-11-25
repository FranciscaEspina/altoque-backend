package com.duoc.altoque.service;

import com.duoc.altoque.dto.AuthResponse;
import com.duoc.altoque.dto.LoginRequest;
import com.duoc.altoque.dto.RegistroRequest;
import com.duoc.altoque.dto.UsuarioDto;
import com.duoc.altoque.dto.UsuarioRequest;
import com.duoc.altoque.entity.RefreshToken;
import com.duoc.altoque.entity.Rol;
import com.duoc.altoque.entity.Usuario;
import com.duoc.altoque.repository.RefreshTokenRepository;
import com.duoc.altoque.repository.UsuarioRepository;
import com.duoc.altoque.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshDias;

    public AuthService(UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository,
                       UsuarioService usuarioService, PasswordEncoder passwordEncoder, JwtService jwtService,
                       @Value("${app.jwt.refresh-expiration-days}") long refreshDias) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshDias = refreshDias;
    }

    @Transactional
    public UsuarioDto registrar(RegistroRequest r) {
        UsuarioRequest req = new UsuarioRequest(r.run(), r.nombre(), r.apellidos(), r.correo(), r.password(),
                r.fechaNacimiento(), r.region(), r.comuna(), r.direccion(), Rol.CLIENTE);
        return usuarioService.crear(req);
    }

    @Transactional
    public AuthResponse login(LoginRequest r) {
        Usuario u = usuarioRepository.findByCorreo(r.correo().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        if (!passwordEncoder.matches(r.password(), u.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return crearRespuesta(u);
    }

    @Transactional
    public AuthResponse refrescar(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));
        if (rt.getExpiracion().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La sesión expiró, inicia sesión nuevamente");
        }
        Usuario u = rt.getUsuario();
        refreshTokenRepository.delete(rt);
        return crearRespuesta(u);
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(String correo) {
        return usuarioRepository.findByCorreo(correo).map(UsuarioDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    }

    private AuthResponse crearRespuesta(Usuario u) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUsuario(u);
        rt.setExpiracion(Instant.now().plus(refreshDias, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);
        return new AuthResponse(jwtService.generarToken(u), rt.getToken(), "Bearer",
                jwtService.getAccessExpirationSeconds(), UsuarioDto.from(u));
    }
}
