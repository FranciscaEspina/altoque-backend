package com.duoc.altoque.service;

import com.duoc.altoque.dto.CrearOrdenRequest;
import com.duoc.altoque.dto.ItemOrdenRequest;
import com.duoc.altoque.dto.OrdenDto;
import com.duoc.altoque.entity.DetalleOrden;
import com.duoc.altoque.entity.Orden;
import com.duoc.altoque.entity.Producto;
import com.duoc.altoque.entity.Rol;
import com.duoc.altoque.entity.Usuario;
import com.duoc.altoque.repository.OrdenRepository;
import com.duoc.altoque.repository.ProductoRepository;
import com.duoc.altoque.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public OrdenService(OrdenRepository ordenRepository, ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.ordenRepository = ordenRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public OrdenDto crear(CrearOrdenRequest r, String correoUsuario) {
        Usuario usuario = correoUsuario == null ? null : usuarioRepository.findByCorreo(correoUsuario).orElse(null);

        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemOrdenRequest item : r.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Orden orden = new Orden();
        orden.setUsuario(usuario);
        orden.setFecha(LocalDateTime.now());
        orden.setEstado("PAGADA");
        orden.setNombre(r.nombre());
        orden.setApellidos(r.apellidos());
        orden.setCorreo(r.correo());
        orden.setCalle(r.calle());
        orden.setDepartamento(r.departamento());
        orden.setRegion(r.region());
        orden.setComuna(r.comuna());
        orden.setIndicaciones(r.indicaciones());

        int total = 0;
        for (Map.Entry<Long, Integer> e : cantidades.entrySet()) {
            Producto p = productoRepository.findByIdForUpdate(e.getKey()).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "El producto " + e.getKey() + " no existe"));
            if (e.getValue() > p.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para " + p.getNombre()
                        + " (solicitado " + e.getValue() + ", disponible " + p.getStock() + ")");
            }
            int precio = p.precioConDescuento();
            int subtotal = precio * e.getValue();

            p.setStock(p.getStock() - e.getValue());

            DetalleOrden d = new DetalleOrden();
            d.setOrden(orden);
            d.setProducto(p);
            d.setNombreProducto(p.getNombre());
            d.setPrecioUnitario(precio);
            d.setCantidad(e.getValue());
            d.setSubtotal(subtotal);
            orden.getDetalles().add(d);
            total += subtotal;
        }
        orden.setTotal(total);
        orden.setNumero(generarNumero());

        return OrdenDto.from(ordenRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenDto> listarTodas() {
        return ordenRepository.findAllByOrderByFechaDesc().stream().map(OrdenDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenDto> listarDeUsuario(String correo) {
        Usuario u = usuarioRepository.findByCorreo(correo).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        return ordenRepository.findByUsuarioOrderByFechaDesc(u).stream().map(OrdenDto::from).toList();
    }

    @Transactional(readOnly = true)
    public OrdenDto obtener(Long id, String correo) {
        Orden o = ordenRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        Usuario solicitante = usuarioRepository.findByCorreo(correo).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
        boolean esPersonal = solicitante.getRol() == Rol.ADMINISTRADOR || solicitante.getRol() == Rol.VENDEDOR;
        boolean esDueno = o.getUsuario() != null && o.getUsuario().getId().equals(solicitante.getId());
        if (!esPersonal && !esDueno) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta orden");
        }
        return OrdenDto.from(o);
    }

    private String generarNumero() {
        String numero;
        do {
            numero = "AT-" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (ordenRepository.existsByNumero(numero));
        return numero;
    }
}
