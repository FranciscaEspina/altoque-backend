package com.duoc.altoque.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "productos")
@Getter @Setter @NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockCritico;

    private String imagen;

    private Boolean enOferta = false;

    private Integer descuento = 0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public int precioConDescuento() {
        int d = descuento == null ? 0 : descuento;
        if (Boolean.TRUE.equals(enOferta) && d > 0) {
            return (int) Math.round(precio * (100 - d) / 100.0);
        }
        return precio;
    }
}
