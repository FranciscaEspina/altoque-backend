package com.duoc.altoque.controller;

import com.duoc.altoque.dto.CategoriaDto;
import com.duoc.altoque.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "CRUD de categorías. Lectura pública; escritura solo ADMINISTRADOR")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Listar categorías")
    @GetMapping
    public List<CategoriaDto> listar() {
        return categoriaService.listar();
    }

    @Operation(summary = "Obtener una categoría por id")
    @GetMapping("/{id}")
    public CategoriaDto obtener(@PathVariable Long id) {
        return categoriaService.obtener(id);
    }

    @Operation(summary = "Crear categoría (solo ADMINISTRADOR)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaDto crear(@Valid @RequestBody CategoriaDto dto) {
        return categoriaService.crear(dto);
    }

    @Operation(summary = "Actualizar categoría (solo ADMINISTRADOR)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CategoriaDto actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto dto) {
        return categoriaService.actualizar(id, dto);
    }

    @Operation(summary = "Eliminar categoría (solo ADMINISTRADOR). Falla si tiene productos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
    }
}
