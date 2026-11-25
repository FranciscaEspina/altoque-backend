package com.duoc.altoque.controller;

import com.duoc.altoque.dto.ProductoDto;
import com.duoc.altoque.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "CRUD de productos. Lectura pública; escritura solo ADMINISTRADOR")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar productos (filtros opcionales: categoriaId, enOferta)")
    @GetMapping
    public List<ProductoDto> listar(@RequestParam(required = false) Long categoriaId,
                                    @RequestParam(required = false) Boolean enOferta) {
        return productoService.listar(categoriaId, enOferta);
    }

    @Operation(summary = "Listar productos con stock crítico (ADMINISTRADOR, VENDEDOR)")
    @GetMapping("/criticos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<ProductoDto> criticos() {
        return productoService.listarCriticos();
    }

    @Operation(summary = "Obtener un producto por id")
    @GetMapping("/{id}")
    public ProductoDto obtener(@PathVariable Long id) {
        return productoService.obtener(id);
    }

    @Operation(summary = "Crear producto (solo ADMINISTRADOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoDto crear(@Valid @RequestBody ProductoDto dto) {
        return productoService.crear(dto);
    }

    @Operation(summary = "Actualizar producto (solo ADMINISTRADOR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ProductoDto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDto dto) {
        return productoService.actualizar(id, dto);
    }

    @Operation(summary = "Eliminar producto (solo ADMINISTRADOR)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
    }
}
