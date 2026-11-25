package com.duoc.altoque.dto;

import com.duoc.altoque.entity.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaDto(
        Long id,
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre) {

    public static CategoriaDto from(Categoria c) {
        return new CategoriaDto(c.getId(), c.getNombre());
    }
}
