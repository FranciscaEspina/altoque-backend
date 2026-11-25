package com.duoc.altoque.config;

import com.duoc.altoque.entity.Categoria;
import com.duoc.altoque.entity.Producto;
import com.duoc.altoque.entity.Rol;
import com.duoc.altoque.entity.Usuario;
import com.duoc.altoque.repository.CategoriaRepository;
import com.duoc.altoque.repository.ProductoRepository;
import com.duoc.altoque.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                           UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Categoria> categorias = new HashMap<>();
        if (categoriaRepository.count() == 0) {
            for (String nombre : new String[]{"Completos", "Sándwiches", "Papas Fritas", "Bebidas"}) {
                categorias.put(nombre, categoriaRepository.save(new Categoria(nombre)));
            }
        } else {
            categoriaRepository.findAll().forEach(c -> categorias.put(c.getNombre(), c));
        }

        if (productoRepository.count() == 0) {
            crearProducto("COMP-001", "Completo italiano", "Vienesa, palta fresca, tomate picado y mayonesa casera.",
                    2200, 40, 5, "/img/completo-italiano.PNG", categorias.get("Completos"), true, 20);
            crearProducto("CHU-001", "Churrasco Al Toque", "Churrasco de carne con nuestros aliños de la casa.",
                    3500, 25, 5, "/img/churrasco-italiano.jpg", categorias.get("Sándwiches"), true, 20);
            crearProducto("PAP-001", "Papas rústicas", "Doble cocción, corte grueso y reposo justo para quedar crujientes.",
                    2800, 30, 6, "/img/papas-rusticas.PNG", categorias.get("Papas Fritas"), false, 0);
            crearProducto("EMP-001", "Empanada de pino", "Empanada horneada rellena de pino tradicional.",
                    1900, 45, 8, "/img/empanadas-de-pino.jpg", categorias.get("Sándwiches"), false, 0);
            crearProducto("BEB-001", "Bebida 350ml", "Bebida a elección, lata de 350ml bien fría.",
                    1200, 60, 10, "/img/bebidas.PNG", categorias.get("Bebidas"), false, 0);
        }

        if (usuarioRepository.count() == 0) {
            crearUsuario("11111111K", "Admin", "Sistema", "admin@duoc.cl", "admin123", Rol.ADMINISTRADOR);
            crearUsuario("22222222K", "Vendedor", "Demo", "vendedor@duoc.cl", "vend123", Rol.VENDEDOR);
            crearUsuario("33333333K", "Cliente", "Demo", "cliente@duoc.cl", "cli123", Rol.CLIENTE);
        }
    }

    private void crearProducto(String codigo, String nombre, String descripcion, int precio, int stock,
                               int stockCritico, String imagen, Categoria categoria, boolean oferta, int descuento) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setStockCritico(stockCritico);
        p.setImagen(imagen);
        p.setCategoria(categoria);
        p.setEnOferta(oferta);
        p.setDescuento(descuento);
        productoRepository.save(p);
    }

    private void crearUsuario(String run, String nombre, String apellidos, String correo, String password, Rol rol) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setContrasena(passwordEncoder.encode(password));
        u.setRol(rol);
        usuarioRepository.save(u);
    }
}
