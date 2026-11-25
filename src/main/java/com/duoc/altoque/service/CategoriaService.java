package com.duoc.altoque.service;

import com.duoc.altoque.dto.CategoriaDto;
import com.duoc.altoque.entity.Categoria;
import com.duoc.altoque.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDto obtener(Long id) {
        return CategoriaDto.from(buscar(id));
    }

    @Transactional
    public CategoriaDto crear(CategoriaDto dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.nombre())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        return CategoriaDto.from(categoriaRepository.save(new Categoria(dto.nombre().trim())));
    }

    @Transactional
    public CategoriaDto actualizar(Long id, CategoriaDto dto) {
        Categoria categoria = buscar(id);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(dto.nombre(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre");
        }
        categoria.setNombre(dto.nombre().trim());
        return CategoriaDto.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        categoriaRepository.delete(buscar(id));
        categoriaRepository.flush();
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }
}
