package com.duoc.altoque.controller;

import com.duoc.altoque.dto.UsuarioDto;
import com.duoc.altoque.dto.UsuarioRequest;
import com.duoc.altoque.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios. Lectura: ADMINISTRADOR y VENDEDOR. Escritura: solo ADMINISTRADOR")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar usuarios (ADMINISTRADOR, VENDEDOR)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<UsuarioDto> listar() {
        return usuarioService.listar();
    }

    @Operation(summary = "Obtener un usuario por id (ADMINISTRADOR, VENDEDOR)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public UsuarioDto obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @Operation(summary = "Crear usuario con cualquier rol (solo ADMINISTRADOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @Operation(summary = "Actualizar usuario (solo ADMINISTRADOR). Contraseña vacía = se conserva la actual")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @Operation(summary = "Eliminar usuario (solo ADMINISTRADOR)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
    }
}
