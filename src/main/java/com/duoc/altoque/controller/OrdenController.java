package com.duoc.altoque.controller;

import com.duoc.altoque.dto.CrearOrdenRequest;
import com.duoc.altoque.dto.OrdenDto;
import com.duoc.altoque.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Órdenes", description = "Compras: crear una orden (valida y descuenta stock) y consultarlas")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @Operation(summary = "Crear orden (visitante o cliente). Valida stock, calcula el total y descuenta el stock")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenDto crear(@Valid @RequestBody CrearOrdenRequest request, Authentication authentication) {
        String correo = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null;
        return ordenService.crear(request, correo);
    }

    @Operation(summary = "Listar todas las órdenes (ADMINISTRADOR, VENDEDOR)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<OrdenDto> listar() {
        return ordenService.listarTodas();
    }

    @Operation(summary = "Historial de compras del usuario autenticado")
    @GetMapping("/mis-ordenes")
    public List<OrdenDto> misOrdenes(Authentication authentication) {
        return ordenService.listarDeUsuario(authentication.getName());
    }

    @Operation(summary = "Obtener una orden (personal o dueño de la orden)")
    @GetMapping("/{id}")
    public OrdenDto obtener(@PathVariable Long id, Authentication authentication) {
        return ordenService.obtener(id, authentication.getName());
    }
}
