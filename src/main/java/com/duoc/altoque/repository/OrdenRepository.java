package com.duoc.altoque.repository;

import com.duoc.altoque.entity.Orden;
import com.duoc.altoque.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findAllByOrderByFechaDesc();
    List<Orden> findByUsuarioOrderByFechaDesc(Usuario usuario);
    boolean existsByNumero(String numero);

    @Modifying
    @Query("update Orden o set o.usuario = null where o.usuario.id = :usuarioId")
    void desvincularUsuario(@Param("usuarioId") Long usuarioId);
}
