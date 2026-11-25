package com.duoc.altoque.dto;

import com.duoc.altoque.entity.DetalleOrden;
import com.duoc.altoque.entity.Orden;

import java.util.List;

public record OrdenDto(
        Long id, String numero, String fecha, String estado, Integer total,
        String nombre, String apellidos, String correo, String calle, String departamento,
        String region, String comuna, String indicaciones, List<DetalleDto> detalles) {

    public record DetalleDto(Long productoId, String nombre, Integer precioUnitario, Integer cantidad, Integer subtotal) {
        public static DetalleDto from(DetalleOrden d) {
            return new DetalleDto(d.getProducto() == null ? null : d.getProducto().getId(),
                    d.getNombreProducto(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal());
        }
    }

    public static OrdenDto from(Orden o) {
        return new OrdenDto(o.getId(), o.getNumero(), o.getFecha().toString(), o.getEstado(), o.getTotal(),
                o.getNombre(), o.getApellidos(), o.getCorreo(), o.getCalle(), o.getDepartamento(),
                o.getRegion(), o.getComuna(), o.getIndicaciones(),
                o.getDetalles().stream().map(DetalleDto::from).toList());
    }
}
