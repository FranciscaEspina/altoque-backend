package com.duoc.altoque.service;

import com.duoc.altoque.dto.UsuarioDto;
import com.duoc.altoque.dto.UsuarioRequest;
import com.duoc.altoque.entity.Rol;
import com.duoc.altoque.entity.Usuario;
import com.duoc.altoque.repository.OrdenRepository;
import com.duoc.altoque.repository.RefreshTokenRepository;
import com.duoc.altoque.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrdenRepository ordenRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository,
                          OrdenRepository ordenRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.ordenRepository = ordenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto obtener(Long id) {
        return UsuarioDto.from(buscar(id));
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest r) {
        if (r.password() == null || r.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (usuarioRepository.existsByRun(r.run())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        if (usuarioRepository.existsByCorreo(r.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        Usuario u = new Usuario();
        aplicar(u, r);
        u.setContrasena(passwordEncoder.encode(r.password()));
        return UsuarioDto.from(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioDto actualizar(Long id, UsuarioRequest r) {
        Usuario u = buscar(id);
        if (usuarioRepository.existsByRunAndIdNot(r.run(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        if (usuarioRepository.existsByCorreoAndIdNot(r.correo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        aplicar(u, r);
        if (r.password() != null && !r.password().isBlank()) {   // vacía = conservar la actual
            u.setContrasena(passwordEncoder.encode(r.password()));
        }
        return UsuarioDto.from(usuarioRepository.save(u));
    }

    @Transactional
    public void eliminar(Long id, String correoActual) {
        Usuario u = buscar(id);
        if (u.getCorreo().equals(correoActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta");
        }
        refreshTokenRepository.deleteByUsuario(u);
        ordenRepository.desvincularUsuario(u.getId());
        usuarioRepository.delete(u);
    }

    private void aplicar(Usuario u, UsuarioRequest r) {
        u.setRun(r.run());
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setCorreo(r.correo().trim());
        u.setFechaNacimiento(r.fechaNacimiento());
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion());
        u.setRol(r.rol() == null ? Rol.CLIENTE : r.rol());
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
