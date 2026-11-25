package com.duoc.altoque.dto;

import com.duoc.altoque.entity.Rol;
import com.duoc.altoque.entity.Usuario;

public record UsuarioDto(
        Long id, String run, String nombre, String apellidos, String correo,
        String fechaNacimiento, String region, String comuna, String direccion, Rol rol) {

    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getFechaNacimiento(), u.getRegion(), u.getComuna(), u.getDireccion(), u.getRol());
    }
}
