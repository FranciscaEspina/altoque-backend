package com.duoc.altoque.dto;

import com.duoc.altoque.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El RUT es obligatorio")
        @Pattern(regexp = "^[0-9]{7,8}[0-9kK]$", message = "RUT sin puntos ni guión (8 o 9 caracteres)")
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "Máximo 50 caracteres")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Máximo 100 caracteres")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(duoc\\.cl|profesor\\.duoc\\.cl|gmail\\.com)$",
                message = "Correo no permitido (@duoc.cl, @profesor.duoc.cl, @gmail.com)")
        String correo,

        @Pattern(regexp = "^$|^.{4,10}$", message = "La contraseña debe tener entre 4 y 10 caracteres")
        String password,

        String fechaNacimiento,
        String region,
        String comuna,

        @Size(max = 300, message = "Máximo 300 caracteres")
        String direccion,

        Rol rol) {
}
