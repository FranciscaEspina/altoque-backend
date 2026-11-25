package com.duoc.altoque.service;

import com.duoc.altoque.dto.ProductoDto;
import com.duoc.altoque.entity.Categoria;
import com.duoc.altoque.entity.Producto;
import com.duoc.altoque.repository.CategoriaRepository;
import com.duoc.altoque.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listar(Long categoriaId, Boolean enOferta) {
        return productoRepository.findAll().stream()
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> enOferta == null || Boolean.TRUE.equals(p.getEnOferta()) == enOferta)
                .map(ProductoDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDto> listarCriticos() {
        return productoRepository.findCriticos().stream().map(ProductoDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductoDto obtener(Long id) {
        return ProductoDto.from(buscar(id));
    }

    @Transactional
    public ProductoDto crear(ProductoDto dto) {
        if (productoRepository.existsByCodigo(dto.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        Producto producto = new Producto();
        aplicar(producto, dto);
        return ProductoDto.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDto actualizar(Long id, ProductoDto dto) {
        Producto producto = buscar(id);
        if (productoRepository.existsByCodigoAndIdNot(dto.codigo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese código");
        }
        aplicar(producto, dto);
        return ProductoDto.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.delete(buscar(id));
        productoRepository.flush();
    }

    private void aplicar(Producto p, ProductoDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoría indicada no existe"));
        p.setCodigo(dto.codigo().trim());
        p.setNombre(dto.nombre().trim());
        p.setDescripcion(dto.descripcion());
        p.setPrecio(dto.precio());
        p.setStock(dto.stock());
        p.setStockCritico(dto.stockCritico());
        p.setImagen(dto.imagen());
        p.setEnOferta(Boolean.TRUE.equals(dto.enOferta()));
        p.setDescuento(dto.descuento() == null ? 0 : dto.descuento());
        p.setCategoria(categoria);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
}
