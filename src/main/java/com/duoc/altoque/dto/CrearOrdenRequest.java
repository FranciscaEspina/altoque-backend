package com.duoc.altoque.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearOrdenRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "Los apellidos son obligatorios") String apellidos,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String correo,
        @NotBlank(message = "La calle es obligatoria") String calle,
        String departamento,
        @NotBlank(message = "La región es obligatoria") String region,
        @NotBlank(message = "La comuna es obligatoria") String comuna,
        @Size(max = 500, message = "Máximo 500 caracteres") String indicaciones,
        @NotEmpty(message = "El carrito no puede estar vacío") @Valid List<ItemOrdenRequest> items) {
}
