package pe.edu.velmore.dao;

import pe.edu.velmore.model.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoDao {
    List<Producto> findAll();
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
}
