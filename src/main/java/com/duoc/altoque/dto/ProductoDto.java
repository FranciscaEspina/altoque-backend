package com.duoc.altoque.dto;

import com.duoc.altoque.entity.Producto;
import jakarta.validation.constraints.*;

public record ProductoDto(
        Long id,

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "Máximo 20 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String nombre,

        @Size(max = 500, message = "Máximo 500 caracteres")
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        Integer precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El stock crítico es obligatorio")
        @Min(value = 0, message = "El stock crítico no puede ser negativo")
        Integer stockCritico,

        String imagen,

        Boolean enOferta,

        @Min(value = 0, message = "El descuento mínimo es 0")
        @Max(value = 90, message = "El descuento máximo es 90")
        Integer descuento,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,
        String categoriaNombre,
        Integer precioFinal) {

    public static ProductoDto from(Producto p) {
        return new ProductoDto(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getStock(), p.getStockCritico(), p.getImagen(), Boolean.TRUE.equals(p.getEnOferta()),
                p.getDescuento() == null ? 0 : p.getDescuento(),
                p.getCategoria().getId(), p.getCategoria().getNombre(), p.precioConDescuento());
    }
}
