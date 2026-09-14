package pe.edu.velmore.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.edu.velmore.model.Producto;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link Producto}.
 * Provee CRUD completo contra MySQL via Spring Data.
 * La caché de Google Guava opera por encima de este repositorio
 * en {@code ProductoServiceImpl}, por lo que este componente
 * solo se invoca en MISS de caché o en escrituras.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /** SELECT * FROM productos WHERE activo = 1 — generado automáticamente. */
    List<Producto> findByActivoTrue();

    /** Búsqueda full-text en nombre y marca, insensible a mayúsculas. */
    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           " LOWER(p.marca)  LIKE LOWER(CONCAT('%', :texto, '%')))")
    List<Producto> buscarPorTexto(String texto);
}
