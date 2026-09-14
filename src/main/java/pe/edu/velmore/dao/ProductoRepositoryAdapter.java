package pe.edu.velmore.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import pe.edu.velmore.model.Producto;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que conecta la interfaz de dominio {@link ProductoDao}
 * con la capa de persistencia JPA {@link ProductoRepository}.
 *
 * <h3>Patrón Adaptador (Gang of Four)</h3>
 * <ul>
 *   <li><b>Target</b>   : {@link ProductoDao}  — lo que conoce el resto del código.</li>
 *   <li><b>Adaptee</b>  : {@link ProductoRepository} — la implementación JPA/MySQL.</li>
 *   <li><b>Adapter</b>  : esta clase — traduce llamadas de ProductoDao a JPA.</li>
 * </ul>
 *
 * <p>Beneficio clave: {@link pe.edu.velmore.service.ProductoServiceImpl} y sus
 * tests unitarios solo conocen {@code ProductoDao}. En tests se inyecta
 * {@link InMemoryProductoDao} (sin BD); en producción Spring inyecta este
 * adaptador que habla con MySQL.</p>
 *
 * <p>{@code @Primary} hace que Spring elija este bean cuando existan varios
 * implementadores de {@code ProductoDao} (InMemoryProductoDao también lo es).</p>
 */
@Primary
@Repository
public class ProductoRepositoryAdapter implements ProductoDao {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepositoryAdapter.class);

    private final ProductoRepository productoRepository;

    public ProductoRepositoryAdapter(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
        log.warn("Producto eliminado de MySQL con ID: {}", id);
    }
}
