package com.duoc.altoque.controller;

import com.duoc.altoque.dto.AuthResponse;
import com.duoc.altoque.dto.LoginRequest;
import com.duoc.altoque.dto.RefreshRequest;
import com.duoc.altoque.dto.RegistroRequest;
import com.duoc.altoque.dto.UsuarioDto;
import com.duoc.altoque.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login con JWT, renovación de token y sesión actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar un cliente (rol CLIENTE)")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @Operation(summary = "Iniciar sesión: devuelve accessToken (JWT) y refreshToken")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Renovar el accessToken usando el refreshToken")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refrescar(request.refreshToken());
    }

    @Operation(summary = "Cerrar sesión: invalida el refreshToken")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
    }

    @Operation(summary = "Datos del usuario autenticado (requiere token)")
    @GetMapping("/me")
    public UsuarioDto me(Authentication authentication) {
        return authService.perfil(authentication.getName());
    }
}
